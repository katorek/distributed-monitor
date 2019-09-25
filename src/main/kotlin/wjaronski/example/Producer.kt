package wjaronski.example

import wjaronski.model.ModelDto
import java.util.concurrent.atomic.AtomicBoolean
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
        var data = Data(updated = AtomicBoolean(actualData.updated.get()), stack = (actualData.stack))
//        var data = actualData
        println("--------------------ENTERING CS------------------------\tS$sharedData\tA$actualData\tData:$data")
        if(data.stack.empty()) {
            data.stack.push("Item_1")
        } else {
            val int = data.stack.lastElement().last().toInt() + 1
            data.stack.push("Item_${int}")
        }
        sleep(1000)
        data.updated.set(true)
        println("++++++++++++++++++++LEAVING CS+++++++++++++++++++++++++\tS$sharedData\tA$actualData\tData:$data")
        releaseSC(data)
        actualData.updated.set(false)
//        data.updated.set(false)
    }

}
