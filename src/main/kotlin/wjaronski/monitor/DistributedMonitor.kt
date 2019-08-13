package wjaronski.monitor

import wjaronski.config.MonitorDto
import wjaronski.message.MessageHandler
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock

class DistributedMonitor(
    override val monitorDto: MonitorDto
//    override val ip: String,
//    override val monitorName: String,
//    override val maxPermits: Int,
//    override val conditionVariablesCount: Int
) : IDistributedMonitor {

    private val _conditionVariables: List<Condition>
    private val _messageHandler = MessageHandler(monitorDto)

    init {
        with(ReentrantLock()) {
            _conditionVariables = generateSequence { newCondition() }.take(monitorDto.conditionVariablesCount).toList()
        }
    }


    override fun await(conditionVariableId: Int) = _conditionVariables[conditionVariableId].await()

    override fun signal(conditionVariableId: Int) {
        _messageHandler
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun signalAll(conditionVariableId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun acquire(permits: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun release(permits: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}