package wjaronski.message

import wjaronski.config.Configuration
import org.zeromq.ZMQ
import org.zeromq.ZMQException
import wjaronski.config.MonitorDto
import java.util.concurrent.atomic.AtomicBoolean

class ProxyHandler(
    val context: ZMQ.Context,
    val shouldEnd: AtomicBoolean,
    val monitorDto: MonitorDto
) : Runnable {

    override fun run() {
        while (!shouldEnd.get()) {
            with(monitorDto) {
                try {

                    val xSub = context.socket(ZMQ.SUB)
                    xSub.bind("tcp://$ip:${proxy.subPort}")
                    // forward all messages
                    xSub.subscribe("".toByteArray())

                    val xPub = context.socket(ZMQ.PUB)
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