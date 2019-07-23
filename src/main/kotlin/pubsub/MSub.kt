package pubsub

import org.zeromq.ZMQ

fun main() {
    val context = ZMQ.context(2)

    val socket = context.socket(ZMQ.XSUB)
    println("connecting to hello world server...")
    socket.connect("tcp://localhost:5897")
    socket.subscribe("A")
//    socket.subscribe(ByteArray(0))

    while (true) {

        val address = socket.recvStr()
        val contents = socket.recvStr()
        println("$address : $contents")
    }
}