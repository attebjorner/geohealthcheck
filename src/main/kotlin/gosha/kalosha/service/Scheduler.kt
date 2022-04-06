package gosha.kalosha.service

import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicBoolean

class Scheduler {

    private val isWorking = AtomicBoolean(true)

    suspend fun schedule(delay: Long, block: suspend () -> Unit) {
        while (isWorking.get()) {
            block()
            delay(delay)
        }
    }

    fun shutdown() {
        isWorking.set(false)
    }
}