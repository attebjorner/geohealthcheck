package gosha.kalosha.service

import gosha.kalosha.properties.*
import gosha.kalosha.service.schedule.Scheduler
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import kotlin.properties.Delegates
import kotlin.test.Test

internal class GeoHealthcheckMonitorTest : AutoCloseKoinTest() {

    private val mutex = Mutex()

    private var numberOfRequests by Delegates.observable(0) { _, _, newValue ->
        if (newValue == numOfRequestsToWait && mutex.isLocked) {
            mutex.unlock()
        }
    }

    private var numOfRequestsToWait = 0

    private val testHealthcheck1 = GeoHealthcheck("servicename1", "80")

    private val testHealthcheck2 = GeoHealthcheck("servicename2", "80")

    private val testHealthcheck3 = GeoHealthcheck("servicename3", "80")

    private val testProperties = AppProperties(
        Logging(Level(LoggingLevel.INFO)),
        Schedule(true, 1),
        ClientServices(setOf()),
        listOf()
    )

    private val scheduler = Scheduler

    private val geoHealthcheckMonitor by inject<GeoHealthcheckMonitor>()

    private val client = HttpClient(MockEngine) {
        engine {
            addHandler { request ->
                ++numberOfRequests
                when (request.url.host) {
                    testHealthcheck1.serviceName -> respond("ok", HttpStatusCode.OK)
                    testHealthcheck2.serviceName -> respond("not ok", HttpStatusCode.InternalServerError)
                    testHealthcheck3.serviceName -> respond("not ok", HttpStatusCode.InternalServerError)
                    else -> error("Unhandled ${request.url.host}")
                }
            }
        }
    }

    private fun startKoin() {
        startKoin {
            modules(
                module {
                    single { testProperties }
                    single { scheduler }
                    single { client }
                    single { GeoHealthcheckMonitor() }
                }
            )
        }
    }

    @Before
    fun setUp() {
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
        numOfRequestsToWait = testProperties.geoHealthchecks.size
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
    fun `should emit false when all geohealthchecks are not OK`() = runBlocking {
        mutex.lock()
        testProperties.geoHealthchecks = listOf(testHealthcheck3, testHealthcheck2)
        numOfRequestsToWait = testProperties.geoHealthchecks.size
        startKoin()
        launch {
            geoHealthcheckMonitor.checkGeoHealthcheckStatus()
                .collectLatest { assertThat(it, equalTo(false)) }
        }
        mutex.lock()
        scheduler.findTask(GEOHEALTHCHECKS_TASK).shutdown()
        mutex.unlock()
    }
}