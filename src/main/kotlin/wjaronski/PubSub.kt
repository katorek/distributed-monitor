package wjaronski


import org.zeromq.ZMQ
import org.zeromq.ZMQException
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.streams.asSequence

/***
 * Constants
 */

val sendMessageTimeSleep = 2_000L
val mainLoopSleepTime = 1_000L
val mainLoopIterations = 1000


private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

private fun randomString(length: Long): String {

    return ThreadLocalRandom.current()
        .ints(length, 0, charPool.size)
        .asSequence()
        .map(charPool::get)
        .joinToString("")
}

val identifier = randomString(5)

fun configurePublisher(
    context: ZMQ.Context,
    shouldEnd: AtomicBoolean,
    isProxyAlive: AtomicBoolean
): Pair<ZMQ.Socket, Thread> {
    val socket = context.socket(ZMQ.PUB)
    socket.connect("tcp://*:6000")

    return Pair(socket, Thread {
        println("Publisher started: $identifier")
        var i = 0
        while (!shouldEnd.get()) {
            val msg1 = "topic A:\t$identifier $i"
            val msg2 = "topic B:\t$identifier $i"

            println("PUB\t$identifier\t${i++}")
            while (!isProxyAlive.get());
            socket.sendMore("A") // topic
            socket.send(msg1)
            socket.sendMore("B") // topic
            socket.send(msg2)

            try {
                Thread.sleep(sendMessageTimeSleep)
            } catch (e: InterruptedException) {
            }

        }
        println("Publisher ended: $identifier")

    })
}


class Subscriber {

}

fun configureSubsriber(context: ZMQ.Context, shouldEnd: AtomicBoolean): Pair<ZMQ.Socket, Thread> {
    val socket = context.socket(ZMQ.SUB)
    socket.connect("tcp://*:6001")
    socket.subscribe("".toByteArray())

    return Pair(socket, Thread {
        try {
            println("Subscriber started: $identifier")
            while (!shouldEnd.get()) {
                val address = socket.recvStr()
                val contents = socket.recvStr()
                println("SUB [$identifier] `$address`\t`$contents`")
            }
            println("Subscriber ended: $identifier")
        } catch (e: ZMQException) {
            println("${e.message}")
        } catch (e: Exception) {
            println("${e.message}")
        }

    })
}


fun configureProxy(context: ZMQ.Context, shouldEnd: AtomicBoolean, isProxyAlive: AtomicBoolean): Thread {

    return Thread {
        while (!shouldEnd.get()) {
            try {
                isProxyAlive.set(false)
                val xSub = context.socket(ZMQ.SUB)
                xSub.bind("tcp://*:6000")
                xSub.subscribe("".toByteArray())

                val xPub = context.socket(ZMQ.PUB)
                xPub.bind("tcp://*:6001")
                println("Configuring proxy")
                isProxyAlive.set(true)
                ZMQ.proxy(xSub, xPub, null)
            } catch (e: ZMQException) { // proxy already configured
                isProxyAlive.set(true)
            }
            try {
                Thread.sleep(mainLoopSleepTime)
            } catch (e: InterruptedException) {
            }
        }
    }

}

fun main() {
    val ctx = ZMQ.context(1)

    val shouldEnd = AtomicBoolean(false)
    val isProxyAlive = AtomicBoolean(false)

    configureProxy(ctx, shouldEnd, isProxyAlive).start()

    val (subSocket, subTh) = configureSubsriber(ctx, shouldEnd)
    val (pubSocket, pubTh) = configurePublisher(ctx, shouldEnd, isProxyAlive)

    subTh.start()
    pubTh.start()

    var i = mainLoopIterations
    while (i-- > 0) {
//        println("$identifier:$i")
        Thread.sleep(mainLoopSleepTime)
    }
    println("Ending !")
    shouldEnd.set(true)
    while (pubTh.isAlive) {
        pubTh.interrupt()
        Thread.sleep(100)
    }

    while (subTh.isAlive) {
        subTh.interrupt()
        Thread.sleep(100)
    }
    println("Finished")

}
