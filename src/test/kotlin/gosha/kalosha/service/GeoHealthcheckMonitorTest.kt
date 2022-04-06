package gosha.kalosha.service

import gosha.kalosha.properties.*
import gosha.kalosha.routing.TEST_NAMESPACE
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import kotlin.test.Test

internal class GeoHealthcheckMonitorTest : AutoCloseKoinTest() {

    private val testHealthcheck1 = GeoHealthcheck("servicename1", "80")

    private val testHealthcheck2 = GeoHealthcheck("servicename2", "80")

    private val testProperties = AppProperties(
        Logging(Level(LoggingLevel.INFO)),
        Schedule(true, 10),
        ClientServices(setOf()),
        listOf()
    )

    private val appStatus = AppStatus(TEST_NAMESPACE)

    private var totalClientRequests = 0

    private val client = HttpClient(MockEngine) {
        engine {
            addHandler { request ->
                ++totalClientRequests
                when (request.url.host) {
                    testHealthcheck1.serviceName -> respond("ok", HttpStatusCode.OK)
                    testHealthcheck2.serviceName -> respond("not ok", HttpStatusCode.InternalServerError)
                    else -> error("Unhandled ${request.url.host}")
                }
            }
        }
    }

    private fun startKoin() {
        startKoin {
            modules(
                module {
                    single { client }
                    single { testProperties }
                    single { appStatus }
                }
            )
        }
    }

    @Before
    fun setUp() {
        stopKoin()
    }

    @Test
    fun `should set geoHealthcheckIsOk to true when geoHealthcheck answers OK`() {
        testProperties.geoHealthcheckList = listOf(testHealthcheck1)
        startKoin()

        runBlocking { GeoHealthcheckMonitor().checkGeoHealthcheckStatus() }
        assertThat(appStatus.geoHealthcheckIsOk.get(), equalTo(true))
    }

    @Test
    fun `should set geoHealthcheckIsOk to false when geoHealthcheck answers not OK`() {
        testProperties.geoHealthcheckList = listOf(testHealthcheck2)
        startKoin()

        runBlocking { GeoHealthcheckMonitor().checkGeoHealthcheckStatus() }
        assertThat(appStatus.geoHealthcheckIsOk.get(), equalTo(false))
    }
}