package wjaronski.message

import org.zeromq.SocketType
import org.zeromq.ZMQ
import wjaronski.config.MonitorDto
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicBoolean

class MessageHandler (
    private val monitorDto: MonitorDto
){

    private val MESSAGE_NEW_THREAD = "NEW"
    private val MESSAGE_REPLY_TO_NEW_THREAD = "REPLY_NEW"

    private val context: ZMQ.Context
    private val proxyRunner: ProxyHandler
    private val shouldEnd = AtomicBoolean(false)

    private val subscriberSocket: ZMQ.Socket
    private val publisherSocket: ZMQ.Socket
    private val localPort: Int
    private val localIp: String

    private val otherThreads: MutableList<LocalThread> = mutableListOf()

    init {
        context = ZMQ.context(3)
        LocalThread.setContext(context)
        proxyRunner = ProxyHandler(context, shouldEnd, monitorDto)

        publisherSocket = monitorPublisherSocket()
        subscriberSocket = monitorSubscriberSocket()

        val address = initLocalAddress()
        localIp = address.first
        localPort = address.second

        initAndStartListener()

        getCurrentListOfThreads()
    }

    private fun initAndStartListener() {
        Thread {
            while (!shouldEnd.get()) {
                val topic = subscriberSocket.recvStr(0)
                val content = subscriberSocket.recvStr(0)
                println("$topic -> $content")
                when(topic) {
                    MESSAGE_NEW_THREAD -> {
                        val (ip, port) = content.split(";")
                        val localThread = LocalThread(ip, port)

                        otherThreads.add(localThread)

                        localThread.sendMessage("$localIp;$localPort")

                    }
                    MESSAGE_REPLY_TO_NEW_THREAD -> {
                        val (ip, port) = content.split(";")
                        //check if thread exists
                        otherThreads.contains(LocalThread(ip, port))
                        otherThreads.add(LocalThread(ip, port))
                    }

                }
            }
        }.start()
    }

    private fun initLocalAddress(): Pair<String, Int> {
        val localSubscriber = context.socket(SocketType.SUB)
        val port = localSubscriber.bindToRandomPort("tcp://*")
        localSubscriber.subscribe("".toByteArray())
        return Pair(InetAddress.getLocalHost().hostAddress, port)
    }

    private fun getCurrentListOfThreads() {
        publisherSocket.sendMore(MESSAGE_NEW_THREAD)
        publisherSocket.send("$localIp;$localPort")
    }

    private fun monitorPublisherSocket(): ZMQ.Socket {
        with(monitorDto) {
            val socket = context.socket(SocketType.PUB)
            socket.connect("tcp://$ip:${proxy.subPort}")
            return socket
        }
    }

    private fun monitorSubscriberSocket(): ZMQ.Socket {
        with(monitorDto){
            val socket = context.socket(SocketType.SUB)
            socket.connect("tcp://$ip:${proxy.pubPort}")
            socket.subscribe(name)
            return socket
        }
    }

    fun sendMessageToAll(message: String) {
        otherThreads.forEach {
            it.sendMessage(message)
        }
    }

}


fun main() {
    println(InetAddress.getLocalHost().hostAddress)
    val x= "123;321"
    val(a,b) = x.split(";")
    println("$a : $b")
}

/*

monitorName topic do subskrypcja





 */