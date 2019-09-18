package wjaronski.message

import java.util.concurrent.atomic.AtomicLong

class LogicalClock {
    private val value = AtomicLong(0)

    @Synchronized
    fun current(): Long {
        return value.get()
    }

    @Synchronized
    fun next(): Long {
        return value.incrementAndGet()
    }

    @Synchronized
    fun sync(value: Long): Long {
        value.coerceAtLeast(value)
        return next()
    }
}
