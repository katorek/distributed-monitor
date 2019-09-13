package test

import org.zeromq.SocketType
import org.zeromq.ZMQ


fun main() {
    val ctx = ZMQ.context(1)
    val s = ctx.socket(SocketType.SUB)
    s.connect("tcp://localhost:6089")
    s.subscribe("".toByteArray())

    while (true) {
        val str = s.recvStr()
        println("a")
        println(str)
    }
}
