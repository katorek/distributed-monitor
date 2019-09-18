package example

import java.util.*

fun main() {
    val pc = Arrays.asList<IProdCons>(
//        Consumer(),
//        Consumer(),
//        Consumer(),
//        Consumer(),
//        Producer(),
//        Producer(),
        Producer(),
        Producer()
    )


    pc.forEach {

        try {
            it.start()
            Thread.sleep(500)
        } catch (e: Exception) {

        }
    }

}
