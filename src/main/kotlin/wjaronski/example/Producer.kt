package wjaronski.example

import wjaronski.model.ModelDto

class Producer : ProdConsImpl(ModelDto.PRODUCER) {


    override fun start() {
        println("\tProducer started")
        super.start()
//        super.run()
        Thread {
            sleep(5000)
            while (true) {
                println("Requesting CS")
                produce()
                sleep(3000)

            }
        }.start()
    }

    public fun produce() {
        requestSC()
        println("--------------------ENTERING CS------------------------")
        sleep(6000)
        println("++++++++++++++++++++LEAVING CS+++++++++++++++++++++++++")
        releaseSC()
    }

}
