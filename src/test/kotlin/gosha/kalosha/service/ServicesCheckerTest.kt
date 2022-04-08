package gosha.kalosha.service

import gosha.kalosha.properties.*
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

internal class ServicesCheckerTest : AutoCloseKoinTest() {

    private val testClientService1 = ClientService("servicename1", "80", "/path")

    private val testClientService2 = ClientService("servicename2", "80", "/path")

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
            }
        )
    }

    @Test
    fun `should execute onSuccess when service responded ok and return check`() = runBlocking {
        var processedService: ClientService? = null
        val check = { true }
        val onSuccess = { service: ClientService -> processedService = service }

        val servicesChecker = ServicesChecker(
            services = setOf(testClientService1),
            url = { service -> "http://${service.serviceName}" },
            onSuccess = onSuccess,
            check = check
        )
        val result = servicesChecker.isStatusUp()
        assertThat(result, equalTo(check()))
        assertThat(processedService, equalTo(testClientService1))
    }

    @Test
    fun `should execute onError when service responded not ok and return check`() = runBlocking {
        var processedService: ClientService? = null
        val check = { true }
        val onError = { service: ClientService -> processedService = service }

        val servicesChecker = ServicesChecker(
            services = setOf(testClientService2),
            url = { service -> "http://${service.serviceName}" },
            onError = onError,
            check = check
        )
        val result = servicesChecker.isStatusUp()
        assertThat(result, equalTo(check()))
        assertThat(processedService, equalTo(testClientService2))
    }
}