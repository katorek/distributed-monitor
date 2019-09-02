package wjaronski.message

import org.zeromq.SocketType
import org.zeromq.ZMQ
import org.zeromq.ZMQException
import wjaronski.config.dto.MonitorDto
import java.util.concurrent.atomic.AtomicBoolean

class ProxyHandler(
    private val context: ZMQ.Context,
    private val shouldEnd: AtomicBoolean,
    private val monitorDto: MonitorDto
) : Runnable {

    override fun run() {
        while (!shouldEnd.get()) {
            with(monitorDto) {
                try {

                    val xSub = context.socket(SocketType.SUB)
                    xSub.bind("tcp://$ip:${proxy.subPort}")
                    // forward all messages
                    xSub.subscribe("".toByteArray())

                    val xPub = context.socket(SocketType.PUB)
                    xPub.bind("tcp://$ip:${proxy.pubPort}")

                    println("Proxy configured")

                    ZMQ.proxy(xSub, xPub, null)

                } catch (e: ZMQException) { // proxy already configured
                }
                try {
                    Thread.sleep(proxy.checkIfAliveTime)
                } catch (e: InterruptedException) {
                }
            }

        }
    }
}