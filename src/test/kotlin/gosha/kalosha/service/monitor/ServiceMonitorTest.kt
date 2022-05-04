package gosha.kalosha.service.monitor

import gosha.kalosha.properties.Service
import gosha.kalosha.service.RequestService
import io.ktor.http.*
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.junit5.AutoCloseKoinTest

internal class ServiceMonitorTest : AutoCloseKoinTest() {

    private val failureThreshold = 2

    private val delay = 1L

    private val upService = spyk(Service(URLBuilder(host = "upService1").buildString(), failureThreshold, delay))

    private val downService = spyk(Service(URLBuilder(host = "downService1").buildString(), failureThreshold, delay))

    private val requestService: RequestService = mockk()

    private val serviceMonitor by inject<ServiceMonitor>()

    private fun startKoin() {
        startKoin {
            modules(
                module {
                    single { requestService }
                    single { ServiceMonitor(get()) }
                }
            )
        }
    }

    @BeforeEach
    fun setUp() {
        coEvery { requestService.updateStatus(any()) } returns Unit
        every { upService.isUp } returns true
        every { downService.isUp } returns false
        stopKoin()
    }

    @Test
    fun `should emit true when checker returns OK`(): Unit = runBlocking {
        startKoin()
        launch {
            serviceMonitor.checkServices(listOf(upService)) { all { isUp -> isUp } }
                .take(1)
                .collectLatest { assertThat(it, equalTo(true)) }
        }
    }

    @Test
    fun `should emit false when checker returns not OK`(): Unit = runBlocking {
        startKoin()
        launch {
            serviceMonitor.checkServices(listOf(downService)) { all { isUp -> isUp } }
                .drop(failureThreshold)
                .take(1)
                .collectLatest { assertThat(it, equalTo(false)) }
        }
    }
}
