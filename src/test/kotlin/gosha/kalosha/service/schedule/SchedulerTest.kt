package gosha.kalosha.service.schedule

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import kotlin.properties.Delegates.observable


internal class SchedulerTest {

    @Test
    fun `should create, execute and shutdown tasks`() = runBlocking {
        val mutex = Mutex(locked = true)
        var isTaskExecuted by observable(false) { _, _, newValue ->
            if (newValue && mutex.isLocked) {
                mutex.unlock()
            }
        }
        val task = Scheduler().createTask("test", 1L) {
            isTaskExecuted = true
        }
        assertThat(task.isActive, equalTo(false))
        launch { task.start() }
        mutex.lock()
        assertThat(isTaskExecuted, equalTo(true))
        assertThat(task.isActive, equalTo(true))
        task.shutdown()
        assertThat(task.isActive, equalTo(false))
        mutex.unlock()
    }

    @Test
    fun `should shutdown all tasks`() = runBlocking {
        val mutex = Mutex(locked = true)
        val scheduler = Scheduler()
        val numberOfTasks = 3
        var numberOfStartedTasks by observable(0) { _, _, newValue ->
            if (newValue == numberOfTasks && mutex.isLocked) {
                mutex.unlock()
            }
        }
        var tasks = setOf<Task>()
        repeat(numberOfTasks) {
            val task = scheduler.createTask("task$it", 1L) { ++numberOfStartedTasks }
            tasks = tasks + task
            assertThat(task.isActive, equalTo(false))
            launch { task.start() }
        }
        mutex.lock()
        for (task in tasks) {
            assertThat(task.isActive, equalTo(true))
        }
        scheduler.shutdownAll()
        for (task in tasks) {
            assertThat(task.isActive, equalTo(false))
        }
        mutex.unlock()
    }
}