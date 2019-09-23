package wjaronski.example

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import wjaronski.model.ModelDto
import kotlin.concurrent.thread

class Producer :
    ProdConsImpl(ModelDto.PRODUCER, sharedData = Data()) {

    override fun start() {
        println("\tProducer started")
        super.start()
        thread(start = true){
            sleep(5000)
            while (true) {
                println("Requesting CS")
                produce()
                sleep(4000)
            }
        }
    }

    fun produce() {
        requestSC()
        var data = actualData
        println("--------------------ENTERING CS------------------------\tS$sharedData\tA$actualData")
        if(data.stack.empty()) {
            data.stack.push("Item_1")
        } else {
            val int = data.stack.lastElement().last().toInt() + 1
            data.stack.push("Item_${int}")
        }
        sleep(1000)
        data.updated.set(true)
        println("++++++++++++++++++++LEAVING CS+++++++++++++++++++++++++\tS$sharedData\tA$actualData")
        releaseSC(data)
        actualData.updated.set(false)
//        data.updated.set(false)
    }

}
