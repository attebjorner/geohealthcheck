package gosha.kalosha.service.schedule

import gosha.kalosha.exception.SchedulerException
import kotlinx.coroutines.delay
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

interface Task {
    val isActive: Boolean
    suspend fun start()
    fun shutdown()
}

class Scheduler {

    private val logger = KotlinLogging.logger {  }

    private val tasks = ConcurrentHashMap<String, Task>()

    fun createTask(name: String, delay: Long, block: suspend () -> Unit): Task {
        if (tasks.containsKey(name)) {
            throw SchedulerException("Task with name $name already exists")
        }
        val task = TaskImpl(name, delay, block)
        tasks[name] = task
        return task
    }

    fun shutdownAll() {
        logger.debug { "Shutting down all tasks" }
        for (task in tasks.values) {
            task.shutdown()
        }
        tasks.clear()
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

    override suspend fun start() {
        logger.debug { "Scheduling task '$name'" }
        isWorking.set(true)
        while (isWorking.get()) {
            block()
            delay(delay)
        }
    }

    override fun shutdown() {
        logger.debug { "Shutting down task '$name'" }
        isWorking.set(false)
    }
}