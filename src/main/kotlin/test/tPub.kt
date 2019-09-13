package test

import org.zeromq.SocketType
import org.zeromq.ZMQ


fun main() {
//    lt.sendMessage("test message")


    val ctx = ZMQ.context(1)
    val s = ctx.socket(SocketType.PUB)
    s.bind("tcp://*:6089")
//    s.subscribe("".toByteArray())

    while (true) {
        println("test")
        s.sendMore("test")
        s.send("test")
        try {
            Thread.sleep(1000)
        } catch (e: Exception) {

        }

    }
}
