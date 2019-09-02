package wjaronski.message

import org.zeromq.SocketType
import org.zeromq.ZMQ
import wjaronski.config.dto.MonitorDto
import wjaronski.message.MessageHandler.Companion.MsgType.*
import wjaronski.monitor.ConditionVariablesManager
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicBoolean

class MessageHandler (
    private val monitorDto: MonitorDto,
    private val locksManager: ConditionVariablesManager
) {

    private val SUBSCRIBTION_TO_ALL_MSGS = "".toByteArray()
    private val MSG_DIVIDER = ";"
    private val INFO_LEVEL = 10

    private val MESSAGE_NEW_THREAD = "NEW"
    private val MESSAGE_REPLY_TO_NEW_THREAD = "REPLY_NEW"


    private val context: ZMQ.Context
    private val proxyThread: Thread
    private val shouldEnd = AtomicBoolean(false)

    private val subscriberSocket: ZMQ.Socket
    private val publisherSocket: ZMQ.Socket
    private val localPort: Int
    private val localIp: String

    private val otherThreads: MutableList<LocalThread> = mutableListOf()

    init {
        context = ZMQ.context(3)
        LocalThread.setContext(context)
        proxyThread = Thread(ProxyHandler(context, shouldEnd, monitorDto))
        proxyThread.start()

        publisherSocket = monitorPublisherSocket()
        subscriberSocket = monitorSubscriberSocket()

        val address = initLocalAddress()
        localIp = address.host
        localPort = address.port

        initAndStartListener()

        getCurrentListOfThreads()
    }

    private fun initAndStartListener() {
        Thread {
            while (!shouldEnd.get()) {
                val topic = subscriberSocket.recvStr(0)
                val content = subscriberSocket.recvStr(0)
                info(1, "$topic -> $content")
                when(topic) {
                    MESSAGE_NEW_THREAD -> {
                        val (ip, port) = content.split(MSG_DIVIDER)
                        val localThread = LocalThread(ip, port)

                        otherThreads.add(localThread)

                        localThread.sendMessage("$localIp;$localPort")

                    }
                    MESSAGE_REPLY_TO_NEW_THREAD -> {
                        val (ip, port) = content.split(MSG_DIVIDER)
                        //check if thread exists
                        otherThreads.contains(LocalThread(ip, port))
                        otherThreads.add(LocalThread(ip, port))
                    }

                }
            }
        }.start()
    }

    private fun info(level: Int, str: String) {
        if (INFO_LEVEL >= level) println(str)
    }

    private fun initLocalAddress(): LocalAddress {
        val localSubscriber = context.socket(SocketType.SUB)
        val port = localSubscriber.bindToRandomPort("tcp://*")
        localSubscriber.subscribe("".toByteArray())
        info(2, "${InetAddress.getLocalHost().hostAddress}:$port")
        return LocalAddress(InetAddress.getLocalHost().hostAddress, port)
    }

    private fun initLocalSubscriberPort() {
        val socket = context.socket(SocketType.SUB)
        socket.connect("tcp://$localIp:$localPort")
        socket.subscribe(SUBSCRIBTION_TO_ALL_MSGS)
        info(1, "initialized local subscriber for all incomin controll messages")

        Thread {
            while (!shouldEnd.get()) {
                val (msgType, msg) = socket.recvStr(0).split(MSG_DIVIDER)
                info(1, "$msgType, $msg")
                when (valueOf(msgType)) {
                    SIGNAL -> {
                        locksManager[msg.toInt()].signal()
                    }
                    SIGNAL_ALL -> {
                        locksManager[msg.toInt()].signalAll()
                    }
                }

            }
        }.start()
    }

    private fun getCurrentListOfThreads() {
        info(1, "getCurrentListOfThreads, '$localIp:$localPort'")
        publisherSocket.sendMore(MESSAGE_NEW_THREAD)
        publisherSocket.send("$localIp$MSG_DIVIDER$localPort")
    }

    private fun monitorPublisherSocket(): ZMQ.Socket {
        with(monitorDto) {
            val socket = context.socket(SocketType.PUB)
            socket.connect("tcp://$ip:${proxy.subPort}")
            info(1, "Created Publisher Socket")
            return socket
        }
    }

    private fun monitorSubscriberSocket(): ZMQ.Socket {
        with(monitorDto){
            val socket = context.socket(SocketType.SUB)
            socket.connect("tcp://$ip:${proxy.pubPort}")
            socket.subscribe(name)
            info(1, "Created Subscriber Socket, Subject '$name'")
            return socket
        }
    }

    fun sendMessageToAll(messageType: MsgType, message: String) {
        info(1, "Sending to all: '$messageType;$message'")
        otherThreads.forEach {
            it.sendMessage("$messageType$MSG_DIVIDER$message")
        }
    }

    fun signal(conditionVariableId: Int) {
        sendMessageToAll(SIGNAL, "$conditionVariableId")
    }

    companion object {
        data class LocalAddress(
            val host: String,
            val port: Int
        )

        enum class MsgType {
            SIGNAL,
            SIGNAL_ALL,
            ACQUIRE,
            RELEASE,
            CS_REQUEST, // Critical Section -> CS
            CS_REPLY
        }
    }

}

/*

monitorName topic do subskrypcja

 */