package example

interface IProdCons {
    fun start()
    //    var monitor: DistributedMonitor
    fun requestSC()

    fun releaseSC()
}
