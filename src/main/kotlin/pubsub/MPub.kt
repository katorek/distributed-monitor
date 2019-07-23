package pubsub

import org.zeromq.ZMQ
import org.zeromq.ZMQException
import java.util.concurrent.ThreadLocalRandom
import kotlin.streams.asSequence

private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

private fun randomString(length: Long): String {
    return ThreadLocalRandom.current()
        .ints(length, 0, charPool.size)
        .asSequence()
        .map(charPool::get)
        .joinToString("")
}

fun main() {
    val identifier = randomString(4)
    val context = ZMQ.context(2)

    val socket = context.socket(ZMQ.XPUB)
    println("connecting to hello world server...")
    try {
        socket.bind("tcp://*:5897")
        println("Connected")
    }catch (e: ZMQException) {
        println("${e.message}\nRetrying...")
        socket.connect("tcp://localhost:5897")
        println("Connected")
    }

    var i = 0
    while (true) {
        val msg1 = "topic A:\t$identifier $i"
        val msg2 = "topic B:\t$identifier $i"

        println("Pub[${Thread.currentThread().id}]: ${i++}, `$msg1` `$msg2`")

        socket.sendMore("A") // topic
        socket.send(msg1)
        socket.sendMore("B") // topic
        socket.send(msg2)
        Thread.sleep(10_000)
    }
}