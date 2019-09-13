package example

import wjaronski.config.Configuration
import wjaronski.model.ModelDto
import wjaronski.monitor.DistributedMonitor

class Producer {
    private val conf = Configuration.invoke()
    private lateinit var monitor: DistributedMonitor

    init {
        Thread {
            monitor = DistributedMonitor(conf.settings.monitor, ModelDto.PRODUCER)
        }.start()
    }
}
