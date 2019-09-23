package wjaronski.monitor

import kotlinx.coroutines.channels.Channel
import wjaronski.config.dto.MonitorDto
import wjaronski.message.MessageHandler

class DistributedMonitor(
    override val monitorDto: MonitorDto,
    val name: String,
    val sharedData: Any

) : IDistributedMonitor {

    private val _locksManager = ConditionVariablesManager(monitorDto)
    private val _messageHandler = MessageHandler(monitorDto, _locksManager, name, sharedData)


    override fun await(conditionVariableId: Int) = _locksManager[conditionVariableId].await()

    override fun signal(conditionVariableId: Int, additionalMessage: String?) {
        _messageHandler.signal(conditionVariableId, additionalMessage)
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

    fun requestCS(data: Any? = null) {
        if (data != null) {
            _messageHandler.algorithm.requestCS()
        }
        _messageHandler.algorithm.requestCS(data)
    }

    fun releaseSC(data: Any? = null) {
        if (data != null) {
            _messageHandler.algorithm.releaseCS()
        }
        _messageHandler.algorithm.releaseCS(data)
    }

//    fun

}
