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
import org.junit.Before
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import kotlin.properties.Delegates.observable
import kotlin.test.Test

internal class ClientClientServiceMonitorTest : AutoCloseKoinTest() {

    private val mutex = Mutex()

    private val failureThreshold = 3

    private var numOfRequestsToWait = 0

    private var numberOfRequests by observable(0) { _, _, newValue ->
        if (newValue == numOfRequestsToWait && mutex.isLocked) {
            mutex.unlock()
        }
    }

    private val testClientService1 = ClientService("servicename1", "80", "/path", failureThreshold)

    private val testClientService2 = ClientService("servicename2", "80", "/path", failureThreshold)

    private val testProperties = AppProperties(
        Logging(Level(LoggingLevel.INFO)),
        Schedule(true, 1),
        ClientServices(setOf(testClientService1, testClientService2)),
        listOf()
    )

    private val scheduler = Scheduler()

    private val client = HttpClient(MockEngine) {
        engine {
            addHandler { request ->
                ++numberOfRequests
                when (request.url.host) {
                    testClientService1.serviceName -> respond("ok", HttpStatusCode.OK)
                    testClientService2.serviceName -> respond("not ok", HttpStatusCode.NotFound)
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
                }
            )
        }
    }

    @Before
    fun setUp() {
        numberOfRequests = 0
        numOfRequestsToWait = failureThreshold
        stopKoin()
    }

    @Test
    fun `should emit true when checker returns OK`() = runBlocking {
        mutex.lock()
        testProperties.clientServices.clientServices = listOf(testClientService1)
        startKoin()
        launch {
            ClientServiceMonitor().checkServices()
                .collectLatest { assertThat(it, equalTo(true)) }
        }
        mutex.lock()
        scheduler.findTask("${CLIENT_SERVICES_TASK}_${testClientService1.serviceName}").shutdown()
        mutex.unlock()
    }

    @Test
    fun `should emit false when checker returns not OK`() = runBlocking {
        mutex.lock()
        testProperties.clientServices.clientServices = listOf(testClientService2)
        startKoin()
        launch {
            ClientServiceMonitor().checkServices()
                .drop(failureThreshold - 1)
                .collectLatest { assertThat(it, equalTo(false)) }
        }
        mutex.lock()
        scheduler.findTask("${CLIENT_SERVICES_TASK}_${testClientService2.serviceName}").shutdown()
        mutex.unlock()
    }
}
