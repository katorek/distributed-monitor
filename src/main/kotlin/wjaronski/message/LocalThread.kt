package wjaronski.message

import org.zeromq.SocketType
import org.zeromq.ZMQ

class LocalThread(
    val ip: String,
    val port: String
) {
    private var socket: ZMQ.Socket? = null

    companion object {
        @JvmField
        var context: ZMQ.Context? = null

        fun setContext(ctx: ZMQ.Context) {
            context = ctx
        }
    }


    fun sendMessage(message: String) {
        if (socket == null) initSocket()
        socket!!.send(message)
    }

    private fun initSocket() {
        socket = context!!.socket(SocketType.PUB)
        socket!!.connect("tcp://$ip:$port")
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