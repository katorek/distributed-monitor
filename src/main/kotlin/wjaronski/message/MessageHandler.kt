package wjaronski.message

import org.zeromq.ZMQ
import wjaronski.config.MonitorDto
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicBoolean

class MessageHandler (
    private val monitorDto: MonitorDto
){

    private val MESSAGE_NEW_THREAD = "NEW"

    private val context: ZMQ.Context
    private val proxyRunner: ProxyHandler
    private val shouldEnd = AtomicBoolean(false)

    private val subscriberSocket: ZMQ.Socket
    private val publisherSocket: ZMQ.Socket
    private val localPort: Int

    private val otherThreads: MutableList<LocalThread> = mutableListOf()

    init {
        context = ZMQ.context(3)
        proxyRunner = ProxyHandler(context, shouldEnd, monitorDto)

        publisherSocket = monitorPublisherSocket()
        subscriberSocket = monitorSubscriberSocket()

        localPort = initLocalPort()
        initAndStartListener()

        getCurrentListOfThreads()
    }

    private fun initAndStartListener() {
//        otherThreads.
        Thread {
            while (!shouldEnd.get()) {
                val topic = subscriberSocket.recvStr(0)
                val content = subscriberSocket.recvStr(0)
                println("$topic -> $content")
                when(topic) {
                    MESSAGE_NEW_THREAD -> {
                        val (ip, port) = content.split(";")
                        otherThreads.add(LocalThread(ip, port))
                    }

                }
            }
        }.start()
    }

    private fun initLocalPort(): Int {
        val localSubscriber = context.socket(ZMQ.SUB)
        val port = localSubscriber.bindToRandomPort("tcp://*")
        localSubscriber.subscribe("".toByteArray())
        return port
    }

    private fun getCurrentListOfThreads() {
        publisherSocket.sendMore(MESSAGE_NEW_THREAD)
        publisherSocket.send("${InetAddress.getLocalHost().hostAddress};$localPort")
    }

    private fun monitorPublisherSocket(): ZMQ.Socket {
        with(monitorDto) {
            val socket = context.socket(ZMQ.PUB)
            socket.connect("tcp://$ip:${proxy.subPort}")
            return socket
        }
    }

    private fun monitorSubscriberSocket(): ZMQ.Socket {
        with(monitorDto){
            val socket = context.socket(ZMQ.SUB)
            socket.connect("tcp://$ip:${proxy.pubPort}")
            socket.subscribe(name)
            return socket
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