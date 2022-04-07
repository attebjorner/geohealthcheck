package gosha.kalosha.service.schedule

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import kotlin.test.Test


internal class SchedulerTest {

    @Test
    fun `should create, execute and shutdown tasks`() = runBlocking {
        var isTaskExecuted = false
        val task = Scheduler.createTask("test", 1L) {
            isTaskExecuted = true
        }
        assertThat(task.isActive, equalTo(false))
        launch { task.schedule() }
        delay(1)
        assertThat(isTaskExecuted, equalTo(true))
        assertThat(task.isActive, equalTo(true))
        task.shutdown()
        assertThat(task.isActive, equalTo(false))
    }

    @Test
    fun `should shutdown all tasks`() = runBlocking {
        val numberOfTasks = 3
        val tasks = mutableSetOf<Task>()
        repeat(numberOfTasks) {
            val task = Scheduler.createTask("task$it", 1L) {}
            tasks.add(task)
            assertThat(task.isActive, equalTo(false))
            launch { task.schedule() }
        }
        delay(1)
        for (task in tasks) {
            assertThat(task.isActive, equalTo(true))
        }
        Scheduler.shutdownAll()
        for (task in tasks) {
            assertThat(task.isActive, equalTo(false))
        }
    }
}