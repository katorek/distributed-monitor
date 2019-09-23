package wjaronski.algorithm

import kotlinx.coroutines.channels.Channel
import wjaronski.message.LocalThread
import wjaronski.message.LogicalClock
import wjaronski.message.Msg
import wjaronski.message.MsgType
import wjaronski.monitor.ConditionVariablesManager
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class RicardAgrawalaAlgorithm(
    override val msgQueue: ConcurrentLinkedQueue<Msg>,
    override val threads: ConcurrentSkipListSet<LocalThread>,
    override val myThread: LocalThread,
    override val locksManager: ConditionVariablesManager,
    override var sharedData: Any
) : IExclusionAlgorithm {
    private val requestingCS = AtomicBoolean(false)
    private var requestingClock: Long = 0
    private val threadsWaitingForReply: LinkedList<LocalThread> = LinkedList()
    private val waitingForRepliesFromThreads: LinkedList<LocalThread> = LinkedList()
    private val clock = LogicalClock()

    override fun requestCS(data: Any?) {
        requestingCS.set(true)
        requestingClock = clock.next()
        val msg = "$requestingClock;$myThread;$data".also { println() }

        waitingForRepliesFromThreads.addAll(threads)
        waitingForRepliesFromThreads.remove(myThread)

        synchronized(threads) {
            println("$myThread\t$requestingClock\t${threads.joinToString(", ", "{", "}")}")
            threads.forEach { it.sendMessage(MsgType.CS_REQUEST, msg) }
        }

        while (waitingForRepliesFromThreads.isNotEmpty()) {
            println(
                "Waiting...{$requestingClock}\t[${waitingForRepliesFromThreads.size} -> ${waitingForRepliesFromThreads.joinToString(
                    ", ",
                    "{",
                    "}"
                )}\t\t\t$myThread"
            )
            Thread.sleep(1000)
        }
        println("\t\t$myThread\t\tcan enter CS")
        clock.next()
    }

    override fun releaseCS(data: Any?) {
        println("Releasing $data")
        synchronized(threadsWaitingForReply) {
            if(data != null) {
                sharedData = data
                threads.forEach { it.sendMessage(MsgType.UPDATE_DATA, data.toString()) }
            }
            requestingCS.set(false)
            threadsWaitingForReply.forEach { sendReply(it) }
            threadsWaitingForReply.clear()
        }
    }

    private fun threadFrom(host: String, port: String): LocalThread? {
        return threads.stream()
            .filter { it.same(host, port) }
            .findFirst()
            .orElse(null)
    }

    private fun threadFrom(str: List<String>): LocalThread {
        return threadFrom(str[0], str[1])!!
    }

    private fun sendReply(lc: LocalThread) {
        val msg = "$myThread"

        lc.sendMessage(MsgType.CS_REPLY, msg)
    }

    override fun msgReplier() {

        thread(start = true) {
            while (true) {
                if (msgQueue.size > 0) {
                    with(msgQueue.poll()) {
                        //                        println("$msgType -> $msg")
                        when (MsgType.valueOf(msgType)) {
                            MsgType.CS_REQUEST -> {
                                val (clockVal, address) = msg.split(";")
                                val lc = threadFrom(address.split(":"))
                                clock.sync(clockVal.toLong())
                                if (requestingCS.get()) {
                                    when {
                                        requestingClock < clockVal.toLong() -> {
                                            //current higher priority
                                            // put on queue waiting threads
                                            threadsWaitingForReply.add(lc)

                                        }
                                        requestingClock == clockVal.toLong() -> when {
                                            myThread < lc -> {
                                                //current higher priority
                                                // put on queue waiting threads
                                                threadsWaitingForReply.add(lc)

                                            }
                                            myThread == lc -> {
                                                //ITS ME,
                                            }
                                            else -> {
                                                // current lower priority
                                                sendReply(lc)
                                            }
                                        }
                                        else -> {
                                            // current lower priority
                                            sendReply(lc)
                                        }
                                    }

                                } else {
                                    sendReply(lc)
                                    // send reply instantly

                                }
                            }
                            MsgType.CS_REPLY -> {
                                val (host, port) = msg.split(":")
                                val lc = threadFrom(host = host, port = port)
                                waitingForRepliesFromThreads.remove(lc)
//                                println(
//                                    "Waiting for: [${waitingForRepliesFromThreads.size}] -> ${waitingForRepliesFromThreads.joinToString(
//                                        ", ",
//                                        "{",
//                                        "}"
//                                    )}"
//                                )
                            }
                            else -> {

                            }

                        }
//                        msgQueue.add(this)
                    }
                }
            }
        }
    }

}
