package wjaronski.example

import wjaronski.model.ModelDto

class Consumer : ProdConsImpl(ModelDto.CONSUMER) {

    override fun start() {
        println("\tConsumer started")

        super.start()
    }

    public fun consume() {

    }

}
