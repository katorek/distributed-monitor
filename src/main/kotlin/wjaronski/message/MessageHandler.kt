package wjaronski.message

import org.zeromq.SocketType
import org.zeromq.ZMQ
import wjaronski.config.Configuration
import wjaronski.config.dto.MonitorDto
import wjaronski.message.MessageHandler.Companion.MsgType.*
import wjaronski.model.ModelDto
import wjaronski.monitor.ConditionVariablesManager
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicBoolean

class MessageHandler (
    private val monitorDto: MonitorDto,
    private val locksManager: ConditionVariablesManager,
    private val iAmProducer: ModelDto = ModelDto.CONSUMER
) {

    private val _uuid: String = "${ThreadLocalRandom.current().nextInt(10, 99)}"

    private val SUBSCRIBTION_TO_ALL_MSGS = "".toByteArray()
    private val MSG_DIVIDER = ";"
    private val INFO_LEVEL = 10

    private val MESSAGE_NEW_THREAD = "NEW"
    private val MESSAGE_REPLY_TO_NEW_THREAD = "REPLY_NEW"


    private val _context: ZMQ.Context
    private val _proxyThread: Thread
    private val _shouldEnd = AtomicBoolean(false)

    private val _subscriberSocket: ZMQ.Socket
    private val _publisherSocket: ZMQ.Socket
    private val _localPort: Int
    private val _localIp: String
    private val _localSub: ZMQ.Socket

    //    private val _otherThreads: MutableList<LocalThread> = mutableListOf()
    private val _otherThreads: ConcurrentSkipListSet<LocalThread> = ConcurrentSkipListSet()
    private val _conf = Configuration

    private val nam: String = iAmProducer.toString() + "__" + _uuid

    init {

        _context = ZMQ.context(10)
        _proxyThread = Thread(ProxyHandler(_context, _shouldEnd, monitorDto))
        _proxyThread.start()

        _publisherSocket = monitorPublisherSocket()
        _subscriberSocket = monitorSubscriberSocket()

        val address = initLocalAddress()
        _localIp = address.host
        _localPort = address.port
        _localSub = address.socket
        initAndStartLocalListenerThread()
        initAndStartProxyMessagesListenerThread()


        getCurrentListOfThreads()

        startProducerConsumer()
    }

    private fun sleep(sec: Int) {
        try {
            println("$nam\tSleeping $sec seconds\t\t\t\t\t\t\t\t:[${_otherThreads.size}]")
            Thread.sleep(sec * 1000L)
        } catch (e: InterruptedException) {
            error(e)
        }
//        println("$nam\tWaked up after $sec seconds")
    }


    private fun sleepRandomTimeBetween(min: Int, max: Int) {
        sleep(ThreadLocalRandom.current().nextInt(min, max))
    }

    private fun startProducerConsumer() {

        while (!_shouldEnd.get()) {
            // if im producer
            //request CS every 5-10 sec

            sleepRandomTimeBetween(5, 10)

//            if(_otherThreads.size < 1) {
//                getCurrentListOfThreads()
//            }

            when (iAmProducer) {
                ModelDto.PRODUCER -> { //producer
//                    println("$nam\tProducing...${_otherThreads.size}")
//                    _publisherSocket.send()
                    sendMessageToAll(CS_REQUEST, "eee makarena xD")

                }
                ModelDto.CONSUMER -> { //consumer
//                    println("$nam\tConsuming...${_otherThreads.size}")
                }
            }
        }

    }

    private fun initAndStartProxyMessagesListenerThread() {
        Thread {
            //            info(1, "initAndStartProxyMessagesListenerThread")
            while (!_shouldEnd.get()) {
                val topic = _subscriberSocket.recvStr()
                val content = _subscriberSocket.recvStr()
                info(1, "$nam\t\t\tPROXY\t\t\t$topic -> $content")
                when(topic) {
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
//        val socket = _context.socket(SocketType.SUB)
//        info(1, "tcp://$_localIp:$_localPort")
//        socket.connect("tcp://*:$_localPort")
//        socket.connect("tcp://$_localIp:$_localPort")
//        socket.subscribe("".toByteArray())
//        info(1, "initialized local subscriber for all incoming control messages")

        Thread {
            while (!_shouldEnd.get()) {
//                val (msgType, msg) = socket.recvStr().split(MSG_DIVIDER)
                val msgType = socket.recvStr()
                val msg = socket.recvStr()
                println("$nam\t\t\tLOCAL\t\t\t$msgType $msg")
                info(1, "$msgType, $msg")
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
                }

            }
        }.start()
    }

    private fun initLocalAddress(): LocalAddress {
        val localSubscriber = _context.socket(SocketType.SUB)
        val port = localSubscriber.bindToRandomPort("tcp://*")
        localSubscriber.subscribe("".toByteArray())
//        info(2, "${InetAddress.getLocalHost().hostAddress}:$port")
        return LocalAddress("localhost", port, localSubscriber)
//        return LocalAddress(InetAddress.getLocalHost().hostAddress, port)
    }

    private fun info(level: Int, str: String) {
        if (INFO_LEVEL >= level) println(str)
    }

    private fun getCurrentListOfThreads() {
        info(1, "$nam\t\t\t\t\t\t\t\t'$_localIp:$_localPort'")
        _publisherSocket.sendMore(MESSAGE_NEW_THREAD)
        _publisherSocket.send("$_localIp$MSG_DIVIDER$_localPort")
    }

    private fun monitorPublisherSocket(): ZMQ.Socket {
        with(monitorDto) {
            val socket = _context.socket(SocketType.PUB)
            socket.connect("tcp://*:${proxy.subPort}")
//            socket.connect("tcp://$ip:${proxy.subPort}")
//            info(1, "tcp://*:${proxy.subPort}")
//            info(1, "Created Publisher Socket")
            return socket
        }
    }

    private fun monitorSubscriberSocket(): ZMQ.Socket {
        with(monitorDto){
            val socket = _context.socket(SocketType.SUB)
            socket.connect("tcp://*:${proxy.pubPort}")
//            socket.connect("tcp://$ip:${proxy.pubPort}")
//            socket.subscribe(name)
            socket.subscribe("".toByteArray())
//            info(1, "tcp://$ip:${proxy.pubPort}")
//            info(1, "Created Subscriber Socket, Subject '$name'")
            return socket
        }
    }

    fun sendMessageToAll(messageType: MsgType, message: String) {
        info(1, "$nam\t\t\t\t\t\t\t\t\t\t\t\t\t\tSending to all: '$messageType;$message'")
        _otherThreads.forEach {
            it.sendMessage(messageType.toString(), message)
        }
    }

    fun signal(conditionVariableId: Int, additionalMessage: String?) {
        sendMessageToAll(SIGNAL, "$conditionVariableId$MSG_DIVIDER$additionalMessage")
    }

    companion object {
        data class LocalAddress(
            val host: String,
            val port: Int,
            val socket: ZMQ.Socket
        )

        enum class MsgType {
            SIGNAL,
            SIGNAL_ALL,
            ACQUIRE,
            RELEASE,
            CS_REQUEST, // CS = Critical Section
            CS_REPLY
        }
    }

}
