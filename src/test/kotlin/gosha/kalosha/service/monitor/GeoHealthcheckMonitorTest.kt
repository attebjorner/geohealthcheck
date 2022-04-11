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
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.inject
import org.koin.test.junit5.AutoCloseKoinTest
import kotlin.properties.Delegates.observable

internal class GeoHealthcheckMonitorTest : AutoCloseKoinTest() {

    private val mutex = Mutex()

    private var numberOfRequests by observable(0) { _, _, newValue ->
        if (newValue == numOfRequestsToWait && mutex.isLocked) {
            mutex.unlock()
        }
    }

    private var numOfRequestsToWait = 0

    private val testHealthcheck1 = GeoHealthcheck(URLBuilder(host = "service1").buildString())

    private val testHealthcheck2 = GeoHealthcheck(URLBuilder(host = "service2").buildString())

    private val testHealthcheck3 = GeoHealthcheck(URLBuilder(host = "service3").buildString())

    private val testProperties = AppProperties(
        Logging(Level(LoggingLevel.INFO)),
        Schedule(true, 1),
        ClientServices(setOf()),
        listOf()
    )

    private val scheduler = Scheduler()

    private val requestService: RequestService = mockk()

    private val geoHealthcheckMonitor by inject<GeoHealthcheckMonitor>()

    private fun startKoin() {
        startKoin {
            modules(
                module {
                    single { testProperties }
                    single { requestService }
                    single { scheduler }
                    single { GeoHealthcheckMonitor(get(), get(), get()) }
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
        numOfRequestsToWait = 1
        stopKoin()
    }

    @Test
    fun `should emit true when checker returns OK`() = runBlocking {
        mutex.lock()
        testProperties.geoHealthchecks = listOf(testHealthcheck1)
        startKoin()
        launch {
            geoHealthcheckMonitor.checkGeoHealthcheckStatus()
                .collectLatest { assertThat(it, equalTo(true)) }
        }
        mutex.lock()
        scheduler.findTask(GEOHEALTHCHECKS_TASK).shutdown()
        mutex.unlock()
    }

    @Test
    fun `should emit false when checker returns not OK`() = runBlocking {
        mutex.lock()
        testProperties.geoHealthchecks = listOf(testHealthcheck2)
        startKoin()
        launch {
            geoHealthcheckMonitor.checkGeoHealthcheckStatus()
                .drop(testHealthcheck2.failureThreshold)
                .collectLatest { assertThat(it, equalTo(false)) }
        }
        mutex.lock()
        scheduler.findTask(GEOHEALTHCHECKS_TASK).shutdown()
        mutex.unlock()
    }

    @Test
    fun `should emit true when at least one geohealthcheck is OK`() = runBlocking {
        mutex.lock()
        testProperties.geoHealthchecks = listOf(testHealthcheck1, testHealthcheck2, testHealthcheck3)
        startKoin()
        numOfRequestsToWait = testProperties.geoHealthchecks.size
        launch {
            geoHealthcheckMonitor.checkGeoHealthcheckStatus()
                .collectLatest { assertThat(it, equalTo(true)) }
        }
        mutex.lock()
        scheduler.findTask(GEOHEALTHCHECKS_TASK).shutdown()
        mutex.unlock()
    }

    @Test
    fun `should emit false when all geohealthchecks are not OK`() = runBlocking {
        mutex.lock()
        testProperties.geoHealthchecks = listOf(testHealthcheck3, testHealthcheck2)
        startKoin()
        numOfRequestsToWait = testProperties.geoHealthchecks.size
        launch {
            geoHealthcheckMonitor.checkGeoHealthcheckStatus()
                .drop(testHealthcheck2.failureThreshold + testHealthcheck3.failureThreshold)
                .collectLatest { assertThat(it, equalTo(false)) }
        }
        mutex.lock()
        scheduler.findTask(GEOHEALTHCHECKS_TASK).shutdown()
        mutex.unlock()
    }
}