package wjaronski.example

interface IProdCons {
    fun start()
    //    var monitor: DistributedMonitor
    fun requestSC(data: Any? = null)

    fun releaseSC(data: Any? = null)
}
