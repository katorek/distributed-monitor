package wjaronski.algorithm

import kotlinx.coroutines.channels.Channel
import wjaronski.message.LocalThread
import wjaronski.message.Msg
import wjaronski.monitor.ConditionVariablesManager
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ConcurrentSkipListSet

interface IExclusionAlgorithm {
    val msgQueue: ConcurrentLinkedQueue<Msg>
    val threads: ConcurrentSkipListSet<LocalThread>
    val myThread: LocalThread
    val locksManager: ConditionVariablesManager
    var sharedData: Any

    fun requestCS(data: Any? = null)
    fun releaseCS(data: Any? = null)
    fun msgReplier()
}
