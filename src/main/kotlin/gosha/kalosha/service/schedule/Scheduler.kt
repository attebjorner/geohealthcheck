package gosha.kalosha.service.schedule

import kotlinx.coroutines.delay
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicBoolean

interface Task {
    val isActive: Boolean
    suspend fun schedule()
    fun shutdown()
}

object Scheduler {

    private val logger = KotlinLogging.logger {  }

    private var tasks = listOf<Task>()

    fun createTask(name: String, delay: Long, block: suspend () -> Unit): Task {
        val task = TaskImpl(name, delay, block)
        tasks = tasks + task
        return task
    }

    fun shutdownAll() {
        logger.info { "Shutting down all tasks" }
        for (task in tasks) {
            task.shutdown()
        }
    }

    private class TaskImpl(
        private val name: String,
        private val delay: Long,
        private val block: suspend () -> Unit
    ) : Task {
        private val logger = KotlinLogging.logger {  }

        private val isWorking = AtomicBoolean(false)

        override val isActive get() = isWorking.get()

        override suspend fun schedule() {
            logger.info { "Scheduling task '$name'" }
            isWorking.set(true)
            while (isWorking.get()) {
                block()
                delay(delay)
            }
        }

        override fun shutdown() {
            logger.info { "Shutting down task '$name'" }
            isWorking.set(false)
        }
    }
}
