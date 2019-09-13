package wjaronski.monitor

import wjaronski.config.dto.MonitorDto
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock

class ConditionVariablesManager(
    private val monitorDto: MonitorDto
) {
    private val _conditionVariables: List<Condition>

    init {
        with(ReentrantLock()) {
            _conditionVariables = generateSequence { newCondition() }.take(monitorDto.conditionVariablesCount).toList()
        }
    }

    operator fun get(id: Int): Condition {
        return _conditionVariables[id]
    }
}