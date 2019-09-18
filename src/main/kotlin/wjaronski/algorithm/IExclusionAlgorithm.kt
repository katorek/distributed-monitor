package wjaronski.algorithm

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

    fun requestCS()
    fun releaseCS()
    fun msgReplier()
}
