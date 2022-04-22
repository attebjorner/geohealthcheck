package gosha.kalosha.service.monitor

import gosha.kalosha.properties.*
import gosha.kalosha.service.RequestService
import gosha.kalosha.service.schedule.Scheduler
import io.ktor.http.*
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
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
import kotlin.properties.Delegates.observable

internal class ServiceMonitorTest : AutoCloseKoinTest() {

    private val failureThreshold = 2

    private val delay = 1L

    private var numOfRequestsToWait = 0

    private var numberOfRequests by observable(0) { _, _, newValue ->
        if (newValue == numOfRequestsToWait) {
            scheduler.shutdownAll()
        }
    }

    private val upService1 = spyk(Service(URLBuilder(host = "upService1").buildString(), failureThreshold, delay))

    private val upService2 = spyk(Service(URLBuilder(host = "upService2").buildString(), failureThreshold, delay))

    private val downService1 = spyk(Service(URLBuilder(host = "downService1").buildString(), failureThreshold, delay))

    private val upHealthcheck1 = spyk(Service(URLBuilder(host = "upHealthcheck1").buildString(), failureThreshold, delay))

    private val downHealthcheck1 = spyk(Service(URLBuilder(host = "downHealthcheck1").buildString(), failureThreshold, delay))

    private val downHealthcheck2 = spyk(Service(URLBuilder(host = "downHealthcheck2").buildString(), failureThreshold, delay))

    private val testProperties = AppProperties(
        Logging(Level(LoggingLevel.INFO)),
        Schedule(true),
        ClientServices(setOf(upService1, downService1)),
        listOf()
    )

    private val scheduler = Scheduler()

    private val requestService: RequestService = mockk()

    private val serviceMonitor by inject<ServiceMonitor>()

    private fun startKoin() {
        startKoin {
            modules(
                module {
                    single { testProperties }
                    single { scheduler }
                    single { requestService }
                    single { ServiceMonitor(get(), get(), get()) }
                }
            )
        }
    }

    @BeforeEach
    fun setUp() {
        coEvery { requestService.updateStatus(any()) } returns Unit
        every { upService1.isUp } answers { ++numberOfRequests; true }
        every { upService2.isUp } answers { ++numberOfRequests; true }
        every { downService1.isUp } answers { ++numberOfRequests; false }
        every { upHealthcheck1.isUp } answers { ++numberOfRequests; true }
        every { downHealthcheck1.isUp } answers { ++numberOfRequests; false }
        every { downHealthcheck2.isUp } answers { ++numberOfRequests; false }
        numberOfRequests = 0
        numOfRequestsToWait = failureThreshold
        stopKoin()
    }

    @Test
    fun `should emit true when checker returns OK`(): Unit = runBlocking {
        testProperties.clientServices.services = listOf(upService1)
        startKoin()
        launch {
            serviceMonitor.checkClientServices()
                .collectLatest { assertThat(it, equalTo(true)) }
        }
    }

    @Test
    fun `should emit false when checker returns not OK`(): Unit = runBlocking {
        testProperties.clientServices.services = listOf(downService1)
        startKoin()
        launch {
            serviceMonitor.checkClientServices()
                .drop(failureThreshold)
                .collectLatest { assertThat(it, equalTo(false)) }
        }
    }

    @Test
    fun `should emit true when all services are up`(): Unit = runBlocking {
        testProperties.clientServices.services = listOf(upService1, upService2)
        startKoin()
        numOfRequestsToWait = testProperties.clientServices.services.size
        launch {
            serviceMonitor.checkClientServices()
                .collectLatest { assertThat(it, equalTo(true)) }
        }
    }

    @Test
    fun `should emit false when any service is down`(): Unit = runBlocking {
        testProperties.clientServices.services = listOf(upService1, upService2, downService1)
        startKoin()
        numOfRequestsToWait = testProperties.clientServices.services.sumOf { it.failureThreshold }
        launch {
            serviceMonitor.checkClientServices()
                .drop(numOfRequestsToWait)
                .collectLatest { assertThat(it, equalTo(false)) }
        }
    }

    @Test
    fun `should emit true when at least one geohealthcheck is OK`(): Unit = runBlocking {
        testProperties.geoHealthchecks = listOf(upHealthcheck1, downHealthcheck1, downHealthcheck2)
        startKoin()
        numOfRequestsToWait = testProperties.geoHealthchecks.size
        launch {
            serviceMonitor.checkGeoHealthchecks()
                .collectLatest { assertThat(it, equalTo(true)) }
        }
    }

    @Test
    fun `should emit false when all geohealthchecks are not OK`(): Unit = runBlocking {
        testProperties.geoHealthchecks = listOf(downHealthcheck2, downHealthcheck1)
        startKoin()
        numOfRequestsToWait = testProperties.geoHealthchecks.sumOf { it.failureThreshold }
        launch {
            serviceMonitor.checkGeoHealthchecks()
                .drop(numOfRequestsToWait)
                .collectLatest { assertThat(it, equalTo(false)) }
        }
    }
}
