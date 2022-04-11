package gosha.kalosha.service.monitor

import gosha.kalosha.properties.*
import gosha.kalosha.service.RequestService
import gosha.kalosha.service.schedule.Scheduler
import io.ktor.http.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.junit5.AutoCloseKoinTest
import kotlin.properties.Delegates.observable

internal class ClientServiceMonitorTest : AutoCloseKoinTest() {

    private val mutex = Mutex()

    private val failureThreshold = 3

    private var numOfRequestsToWait = 0

    private var numberOfRequests by observable(0) { _, _, newValue ->
        if (newValue == numOfRequestsToWait && mutex.isLocked) {
            mutex.unlock()
        }
    }

    private val testService1 = ClientService(URLBuilder(host = "service1").buildString(), failureThreshold)

    private val testService2 = ClientService(URLBuilder(host = "service2").buildString(), failureThreshold)

    private val testProperties = AppProperties(
        Logging(Level(LoggingLevel.INFO)),
        Schedule(true, 1),
        ClientServices(setOf(testService1, testService2)),
        listOf()
    )

    private val scheduler = Scheduler()

    private val requestService: RequestService = mockk()

    private val clientServiceMonitor by inject<ClientServiceMonitor>()

    private fun startKoin() {
        startKoin {
            modules(
                module {
                    single { testProperties }
                    single { scheduler }
                    single { requestService }
                    single { ClientServiceMonitor(get(), get(), get()) }
                }
            )
        }
    }

    @BeforeEach
    fun setUp() {
        coEvery { requestService.updateStatus(any()) } answers {
            ++numberOfRequests
        }
        numberOfRequests = 0
        numOfRequestsToWait = failureThreshold
        stopKoin()
    }

    @Test
    fun `should emit true when checker returns OK`() = runBlocking {
        mutex.lock()
        testProperties.clientServices.services = listOf(testService1)
        startKoin()
        launch {
            clientServiceMonitor.checkServices()
                .collectLatest { assertThat(it, equalTo(true)) }
        }
        mutex.lock()
        scheduler.findTask("${CLIENT_SERVICES_TASK}_${testService1.endpoint}").shutdown()
        mutex.unlock()
    }

    @Test
    fun `should emit false when checker returns not OK`() = runBlocking {
        mutex.lock()
        testProperties.clientServices.services = listOf(testService2)
        startKoin()
        launch {
            clientServiceMonitor.checkServices()
                .drop(failureThreshold)
                .collectLatest { assertThat(it, equalTo(false)) }
        }
        mutex.lock()
        scheduler.findTask("${CLIENT_SERVICES_TASK}_${testService2.endpoint}").shutdown()
        mutex.unlock()
    }
}
