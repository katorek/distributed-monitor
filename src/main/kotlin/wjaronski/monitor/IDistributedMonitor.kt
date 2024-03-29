package wjaronski.monitor

import wjaronski.config.dto.MonitorDto

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
    fun signal(conditionVariableId: Int, additionalMessage: String?)
    fun signalAll(conditionVariableId: Int)

}