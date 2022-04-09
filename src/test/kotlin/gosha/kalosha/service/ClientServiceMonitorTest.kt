package gosha.kalosha.service

import gosha.kalosha.properties.*
import gosha.kalosha.service.schedule.Scheduler
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
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

    private val testService1 = Service("servicename1", "80", "/path", failureThreshold)

    private val testService2 = Service("servicename2", "80", "/path", failureThreshold)

    private val testProperties = AppProperties(
        Logging(Level(LoggingLevel.INFO)),
        Schedule(true, 1),
        ClientServices(setOf(testService1, testService2)),
        listOf()
    )

    private val scheduler = Scheduler()

    private val clientServiceMonitor by inject<ClientServiceMonitor>()

    private val client = HttpClient(MockEngine) {
        engine {
            addHandler { request ->
                ++numberOfRequests
                when (request.url.host) {
                    testService1.serviceName -> respond("ok", HttpStatusCode.OK)
                    testService2.serviceName -> respond("not ok", HttpStatusCode.NotFound)
                    else -> error("Unhandled ${request.url.host}")
                }
            }
        }
    }

    private fun startKoin() {
        org.koin.core.context.startKoin {
            modules(
                module {
                    single { testProperties }
                    single { scheduler }
                    single { client }
                    single { ClientServiceMonitor(get(), get(), get()) }
                }
            )
        }
    }

    @BeforeEach
    fun setUp() {
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
        scheduler.findTask("${CLIENT_SERVICES_TASK}_${testService1.serviceName}").shutdown()
        mutex.unlock()
    }

    @Test
    fun `should emit false when checker returns not OK`() = runBlocking {
        mutex.lock()
        testProperties.clientServices.services = listOf(testService2)
        startKoin()
        launch {
            clientServiceMonitor.checkServices()
                .drop(failureThreshold - 1)
                .collectLatest { assertThat(it, equalTo(false)) }
        }
        mutex.lock()
        scheduler.findTask("${CLIENT_SERVICES_TASK}_${testService2.serviceName}").shutdown()
        mutex.unlock()
    }
}
