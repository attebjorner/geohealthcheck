package gosha.kalosha.service

import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


object Scheduler : KoinComponent {

    private val scope by inject<CoroutineScope>()

    fun schedule(repeatMillis: Long, block: suspend () -> Unit) {
        scope.launch {
            if (repeatMillis > 0) {
                while (isActive) {
                    block()
                    delay(repeatMillis)
                }
            } else {
                block()
            }
        }
    }
}