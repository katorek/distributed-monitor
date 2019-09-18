package wjaronski.message

import org.zeromq.SocketType
import org.zeromq.ZMQ

class LocalThread(
    val ip: String,
    val port: String,
    val context: ZMQ.Context
) : Comparable<LocalThread> {
    constructor(address: MessageHandler.Companion.LocalAddress) : this(
        address.host,
        address.port.toString(),
        address.context!!
    )

    override fun compareTo(o: LocalThread): Int {
        val ips = ip.compareTo(o.ip)
        val ports = port.compareTo(o.port)
        if (ips == 0 && ports == 0) return 0
        else if (ips == 0) return ports
        return ips
    }

    private lateinit var socket: ZMQ.Socket


    fun init(str: String) {
        println("$str\t\tLOCALTHREAD\t\t$port")
        initSocket()
    }

    fun sendMessage(msgType: String, msg: String) {
//        if(socket == null ) initSocket()
        socket.sendMore(msgType)
        socket.send(msg)
    }

    fun sendMessage(msgType: MsgType, msg: String) {
        sendMessage(msgType.toString(), msg)
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


    override fun toString(): String {
        return "$ip:$port"
    }

    fun same(host: String, port: String): Boolean {
        return (ip.equals(host) && this.port.equals(port))
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
