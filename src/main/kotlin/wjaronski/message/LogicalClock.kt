package wjaronski.message

import java.util.concurrent.atomic.AtomicInteger

class LogicalClock {
    private val value = AtomicInteger(0)

    @Synchronized
    fun current(): Int {
        return value.get()
    }

    @Synchronized
    fun next(): Int {
        return value.incrementAndGet()
    }

    @Synchronized
    fun sync(value: Int): Int {
        value.coerceAtLeast(value)
        return next()
    }
}