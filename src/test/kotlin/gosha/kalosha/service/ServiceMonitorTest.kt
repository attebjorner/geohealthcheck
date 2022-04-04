package gosha.kalosha.service

import gosha.kalosha.config.*
import gosha.kalosha.routing.TEST_NAMESPACE
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import kotlin.test.Test

internal class ServiceMonitorTest : KoinTest {

    private val testService1 = Service("servicename1", "80", "/path")

    private val testService2 = Service("servicename2", "80", "/path")

    private val testProperties = AppProperties(
        Logging(Level(LoggingLevel.INFO)),
        Schedule(true, 10),
        ClientServices(listOf(testService1, testService2)),
        failureThreshold = 3,
        GeoHealthcheck("serviceName", "port")
    )

    private val appStatus = AppStatus(TEST_NAMESPACE)

    private val client = HttpClient(MockEngine) {
        engine {
            addHandler { request ->
                if (request.url.host == testService1.serviceName) {
                    respond("ok", HttpStatusCode.OK)
                } else if (request.url.host == testService2.serviceName) {
                    respond("notok", HttpStatusCode.NotFound)
                } else {
                    error("Unhandled ${request.url.host}")
                }
            }
        }
    }

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(
            module {
                single { client }
                single { testProperties }
                single { appStatus }
            }
        )
    }

    @Test
    fun `should set appStatus#isOk to false when any service returned not 200 #failureThreshold times`() {
        assertThat(appStatus.isOk, equalTo(true))
        runBlocking { ServiceMonitor.monitor() }
        assertThat(appStatus.isOk, equalTo(false))
    }
}