package wjaronski.monitor

import wjaronski.config.dto.MonitorDto
import wjaronski.message.MessageHandler
import wjaronski.model.ModelDto

class DistributedMonitor(
    override val monitorDto: MonitorDto,
    var model: ModelDto = ModelDto.CONSUMER
) : IDistributedMonitor {

    private val _locksManager = ConditionVariablesManager(monitorDto)
    private val _messageHandler = MessageHandler(monitorDto, _locksManager, model)


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

}
