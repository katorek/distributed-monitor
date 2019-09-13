package wjaronski.message

import org.zeromq.SocketType
import org.zeromq.ZMQ

class LocalThread(
    val ip: String,
    val port: String,
    val context: ZMQ.Context
) : Comparable<LocalThread> {
    override fun compareTo(other: LocalThread): Int {
        if (this.equals(other)) return 0
        return 1
    }

    private lateinit var socket: ZMQ.Socket

//    companion object {
//        @JvmField
//        var context: ZMQ.Context? = null
//
//        fun setContext(ctx: ZMQ.Context) {
//            context = ctx
//        }
//    }

    fun init(str: String) {
        println("$str\t\tLOCALTHREAD\t$port")
        initSocket()
    }

    fun sendMessage(msgType: String, msg: String) {
        socket.sendMore(msgType)
        socket.send(msg)
    }

    private fun initSocket() {
        socket = context.socket(SocketType.PUB)
        socket.connect("tcp://*:$port")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocalThread

        if (ip != other.ip) return false
        if (port != other.port) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ip.hashCode()
        result = 31 * result + port.hashCode()
        return result
    }


}


fun main() {
//    LocalThread.setContext(ZMQ.context(10))
    val ctx = ZMQ.context(10)
    val lt = LocalThread("localhost", "60838", ZMQ.context(10))

    lt.init("test")
    lt.sendMessage("test message", "")
}

fun main2() {
    val ctx = ZMQ.context(10)
    val s = ctx.socket(SocketType.SUB)
    s.connect("tcp://*:60838")
    s.subscribe("".toByteArray())

    while (true) {
        println(s.recvStr())
    }
}
