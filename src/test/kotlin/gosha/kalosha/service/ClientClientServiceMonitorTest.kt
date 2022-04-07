package gosha.kalosha.service

import gosha.kalosha.properties.*
import gosha.kalosha.routing.TEST_NAMESPACE
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.KoinTestRule
import kotlin.test.Test

internal class ClientClientServiceMonitorTest : AutoCloseKoinTest() {

    private val failureThreshold = 3

    private val testClientService1 = ClientService("servicename1", "80", "/path", failureThreshold)

    private val testClientService2 = ClientService("servicename2", "80", "/path", failureThreshold)

    private val testProperties = AppProperties(
        Logging(Level(LoggingLevel.INFO)),
        Schedule(true, 10),
        ClientServices(setOf(testClientService1, testClientService2)),
        listOf()
    )

    private val appStatus = AppStatus(TEST_NAMESPACE)

    private val client = HttpClient(MockEngine) {
        engine {
            addHandler { request ->
                when (request.url.host) {
                    testClientService1.serviceName -> respond("ok", HttpStatusCode.OK)
                    testClientService2.serviceName -> respond("not ok", HttpStatusCode.NotFound)
                    else -> error("Unhandled ${request.url.host}")
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
    fun `should set appStatus#isOk to false when any service returned not 200 #failureThreshold times and geoHealthcheck returned OK and not continue work`() {
        appStatus.geoHealthcheckIsOk.set(true)
        assertThat(appStatus.isOk.get(), equalTo(true))
        repeat(failureThreshold) {
            runBlocking { ClientServiceMonitor().checkServices() }
        }
        assertThat(appStatus.isOk.get(), equalTo(false))
    }

    @Test
    fun `should set appStatus#isOk to true when any service returned not 200 #failureThreshold times and geoHealthcheck returned 500 and continue work`() {
        appStatus.geoHealthcheckIsOk.set(false)
        assertThat(appStatus.isOk.get(), equalTo(true))
        repeat(failureThreshold) {
            runBlocking { ClientServiceMonitor().checkServices() }
        }
        assertThat(appStatus.isOk.get(), equalTo(true))
    }
}
