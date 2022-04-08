package gosha.kalosha.service.schedule

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

interface Task {
    val isActive: Boolean
    suspend fun schedule()
    fun shutdown()
}

class Scheduler {

    private val logger = KotlinLogging.logger {  }

    private val tasks = ConcurrentHashMap<String, Task>()

    fun createTask(name: String, delay: Long, block: suspend () -> Unit): Task {
        val task = TaskImpl(name, delay, block)
        tasks[name] = task
        return task
    }

    fun shutdownAll() {
        logger.info { "Shutting down all tasks" }
        for (task in tasks.values) {
            task.shutdown()
        }
    }

    fun findTask(name: String): Task =
        tasks[name] ?: throw RuntimeException("Task with name $name doesn't exist")
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