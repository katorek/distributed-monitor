package wjaronski.example

import wjaronski.config.Configuration
import wjaronski.model.ModelDto
import wjaronski.monitor.DistributedMonitor
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

open class ProdConsImpl(
    private val pc: ModelDto,
    val sharedData: Data
) : IProdCons {
    var actualData: Data = Data()


    override fun start() {
        monitor = DistributedMonitor(conf.settings.monitor, pc.toString(), sharedData)
        intited = true
        syncData()
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

    override fun requestSC(data: Any?) {
        while (!this.intited);
//            monitor.requestCS()
        monitor.requestCS(data)
    }

    override fun releaseSC(data: Any?) {
//        if (data == null) {
//            monitor.requestCS()
//        }
        monitor.releaseSC(data)
    }

    fun syncData() {
        thread(start = true) {
            while (true) {
                synchronized(sharedData.updated){
                    if(sharedData.updated.get()){
                        println("Moving Shared to Actual\t\tS_$sharedData -> A_$actualData")
                        actualData = sharedData
                        sharedData.updated.set(false)
                    }
                }
                sleep(100)
            }
        }
    }

}

data class Data(
    val updated: AtomicBoolean = AtomicBoolean(false),
    val stack: Stack<String> = Stack<String>()
)
