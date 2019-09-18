package wjaronski.example

import wjaronski.config.Configuration
import wjaronski.model.ModelDto
import wjaronski.monitor.DistributedMonitor

open class ProdConsImpl(
    private val pc: ModelDto
) : IProdCons {

    override fun start() {
        monitor = DistributedMonitor(conf.settings.monitor, pc)
        intited = true
    }

    //    private val th: Thread
    private val conf = Configuration.invoke()
    lateinit var monitor: DistributedMonitor
    private var intited = false

    fun sleep(time: Long) {
        try {
            Thread.sleep(time)
        } catch (e: Exception) {
        }
    }

    override fun requestSC() {
        while (!this.intited);
        monitor.requestCS()
    }

    override fun releaseSC() {
        monitor.releaseSC()
    }

//    override fun run() {
//        println("started")
////        th.start()
//    }


}
