package wjaronski.monitor

import wjaronski.config.Configuration
import wjaronski.config.MonitorDto
import java.util.concurrent.locks.Condition

interface IDistributedMonitor {
    val monitorDto: MonitorDto
//    val ip: String
//    val monitorName: String
//    val maxPermits: Int
//    val conditionVariablesCount: Int
//    val conditionList: List<Condition>

//    val conditionVariables: List<Boolean>
    fun acquire(permits: Int)
    fun release(permits: Int)

//    todo later
//    fun acquireExclusive()
//    fun releaseExclusive()

    fun await(conditionVariableId: Int)
    fun signal(conditionVariableId: Int)
    fun signalAll(conditionVariableId: Int)

}