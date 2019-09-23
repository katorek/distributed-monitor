package wjaronski.example

import kotlinx.coroutines.channels.Channel
import wjaronski.model.ModelDto

class Consumer :
    ProdConsImpl(ModelDto.CONSUMER, Data()) {

    override fun start() {
        println("\tConsumer started")

        super.start()
    }

    public fun consume() {

    }

}
