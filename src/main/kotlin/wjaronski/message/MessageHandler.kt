package wjaronski.message

import org.zeromq.SocketType
import org.zeromq.ZMQ
import wjaronski.algorithm.IExclusionAlgorithm
import wjaronski.config.Configuration
import wjaronski.config.dto.MonitorDto
import wjaronski.message.MsgType.*
import wjaronski.monitor.ConditionVariablesManager
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class MessageHandler(
    private val monitorDto: MonitorDto,
    private val locksManager: ConditionVariablesManager,
    private val name: String,
    @Volatile var sharedData: Any
) {

    private val _uuid: String = "${ThreadLocalRandom.current().nextInt(10, 99)}"

    private val SUBSCRIBTION_TO_ALL_MSGS = "".toByteArray()
    private val MSG_DIVIDER = ";"
    private val INFO_LEVEL = 10

    private val MESSAGE_NEW_THREAD = "NEW"
    private val MESSAGE_REPLY_TO_NEW_THREAD = "REPLY_NEW"


    private val _context: ZMQ.Context
    private val _shouldEnd = AtomicBoolean(false)

    private val _subscriberSocket: ZMQ.Socket
    private val _publisherSocket: ZMQ.Socket
    private val _localPort: Int
    private val _localIp: String
    private val _localSub: ZMQ.Socket

    //    private val _otherThreads: MutableList<LocalThread> = mutableListOf()
    private val _otherThreads: ConcurrentSkipListSet<LocalThread> = ConcurrentSkipListSet()
    private val _conf = Configuration

    private val nam: String
    private val msgQueue: ConcurrentLinkedQueue<Msg> = ConcurrentLinkedQueue()
    var algorithm: IExclusionAlgorithm

    init {

        _context = ZMQ.context(10)
        var _proxyThread = Thread(ProxyHandler(_context, _shouldEnd, monitorDto))
        _proxyThread.start()

        _publisherSocket = monitorPublisherSocket()
        _subscriberSocket = monitorSubscriberSocket()

        val address = initLocalAddress()
        _localIp = address.host
        _localPort = address.port
        _localSub = address.socket
        nam = "${name}__$_localPort"
        address.context = _context

        val myThread = LocalThread(address)
        myThread.init(nam)
        _otherThreads.add(myThread)
        val alg = _conf.invoke().settings.monitor.algorithm
        val algClass = Class.forName(alg)
        print("", "Using ${algClass.simpleName} as exclusion algorithm")

        algorithm = algClass.constructors.first().newInstance(
            msgQueue,
            _otherThreads,
            myThread,
            locksManager,
            sharedData
        ) as IExclusionAlgorithm

        initAndStartLocalListenerThread()
        initAndStartProxyMessagesListenerThread()


        getCurrentListOfThreads()
        algorithm.msgReplier()
    }

    private fun sleep(sec: Int) {
        try {
            print(
                "",
                "Sleep[$sec]",
                "",
                "",
                "",
                _otherThreads.joinToString(";", "${_otherThreads.size.toString()} ->  {", "}")
            )
//            println("$nam\tSleeping $sec seconds\t\t\t\t\t\t\t\t:[${_otherThreads.size}]")
            Thread.sleep(sec * 1000L)
        } catch (e: InterruptedException) {
            error(e)
        }
//        println("$nam\tWaked up after $sec seconds")
    }

    private fun sleepRandomTimeBetween(min: Int, max: Int) {
        sleep(ThreadLocalRandom.current().nextInt(min, max))
    }

    private fun initAndStartProxyMessagesListenerThread() {
        Thread {
            while (!_shouldEnd.get()) {
                val topic = _subscriberSocket.recvStr()
                val content = _subscriberSocket.recvStr()
                print("PROXY", topic, content)

                when (topic) {
                    MESSAGE_NEW_THREAD -> {
                        val (ip, port) = content.split(MSG_DIVIDER)
                        val dto = LocalThread(ip, port, _context)
                        if (!_otherThreads.contains(dto)) {
                            dto.init(nam)
                            _otherThreads.add(dto)
                        }

                        _publisherSocket.sendMore(MESSAGE_REPLY_TO_NEW_THREAD)
                        _publisherSocket.send("$_localIp$MSG_DIVIDER$_localPort")

                    }
                    MESSAGE_REPLY_TO_NEW_THREAD -> {
                        val (ip, port) = content.split(MSG_DIVIDER)
                        val dto = LocalThread(ip, port, _context)
                        if (!_otherThreads.contains(dto)) {
                            dto.init(nam)
                            _otherThreads.add(dto)
                        }
                    }

                }
            }
        }.start()
    }

    private fun initAndStartLocalListenerThread() {
        val socket = _localSub

        thread(start = true) {
            while (!_shouldEnd.get()) {
                val msgType = socket.recvStr()
                val msg = socket.recvStr()
//                print("LOCAL", msgType, msg)
                when (valueOf(msgType)) {
                    CS_REQUEST -> {
                        msgQueue.add(Msg(msgType, msg))
                    }
                    CS_REPLY -> {
                        msgQueue.add(Msg(msgType, msg))
                    }
                    UPDATE_DATA -> {
                        println("$nam\tUPDATING_DATA with: $msg")
                        sharedData = msg
                    }
                    else -> {

                    }
                }
//                msgQueue.add(Msg(msgType, msg))

                when (valueOf(msgType)) {
//                    MESSAGE_NEW_THREAD -> {

//                    }
                    SIGNAL -> {
                        locksManager[msg.toInt()].signal()
                    }
                    SIGNAL_ALL -> {
                        locksManager[msg.toInt()].signalAll()
                    }
                    CS_REQUEST -> {
//                        println("$nam\t\t${CS_REQUEST}\t\t$msg")
                    }
                    else -> {

                    }
                }

            }
        }
    }

    private fun initLocalAddress(): LocalAddress {
        val localSubscriber = _context.socket(SocketType.SUB)
        val port = localSubscriber.bindToRandomPort("tcp://*")
        localSubscriber.subscribe(SUBSCRIBTION_TO_ALL_MSGS)
//        info(2, "${InetAddress.getLocalHost().hostAddress}:$port")
        return LocalAddress("localhost", port, localSubscriber)
//        return LocalAddress(InetAddress.getLocalHost().hostAddress, port)
    }

    private fun print(vararg msgs: String) {
//        println(msgs.fold("$nam\t\t\t") { s1, s2 -> "$s1\t\t$s2" })
    }

    private fun info(level: Int, m1: String, m2: String = "", m3: String = "") {
        if (INFO_LEVEL >= level) println("$nam\t\t\t$m1\t\t$m2\t\t$m3")
    }

    private fun getCurrentListOfThreads() {
        thread(start = true) {
            print("", "", "'$_localIp:$_localPort'")
            _publisherSocket.sendMore(MESSAGE_NEW_THREAD)
            _publisherSocket.send("$_localIp$MSG_DIVIDER$_localPort")
            println("${_otherThreads.size}")
            while (_otherThreads.size < 2) {
                println("${_otherThreads.size}")
                sleepRandomTimeBetween(1, 2)
                _publisherSocket.sendMore(MESSAGE_NEW_THREAD)
                _publisherSocket.send("$_localIp$MSG_DIVIDER$_localPort")
            }
        }
    }

    private fun monitorPublisherSocket(): ZMQ.Socket {
        with(monitorDto) {
            val socket = _context.socket(SocketType.PUB)
            socket.connect("tcp://*:${proxy.subPort}")
            return socket
        }
    }

    private fun monitorSubscriberSocket(): ZMQ.Socket {
        with(monitorDto) {
            val socket = _context.socket(SocketType.SUB)
            socket.connect("tcp://*:${proxy.pubPort}")
            socket.subscribe(SUBSCRIBTION_TO_ALL_MSGS)
            return socket
        }
    }

    fun sendMessageToAll(messageType: MsgType, message: String) {
        print("", "Send All", "", "'$messageType;$message'")
        _otherThreads.forEach {
            it.sendMessage(messageType.toString(), message)
        }
    }

    fun signal(conditionVariableId: Int, additionalMessage: String?) {
        sendMessageToAll(MsgType.SIGNAL, "$conditionVariableId$MSG_DIVIDER$additionalMessage")
    }

    companion object {
        data class LocalAddress(
            val host: String,
            val port: Int,
            val socket: ZMQ.Socket,
            var context: ZMQ.Context? = null
        )

    }


}
