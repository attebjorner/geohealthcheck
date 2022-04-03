package gosha.kalosha.routing

import gosha.kalosha.config.*
import io.ktor.http.*
import io.ktor.application.*
import kotlin.test.*
import io.ktor.server.testing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.koin.dsl.module
import org.koin.ktor.ext.Koin
import org.koin.logger.SLF4JLogger
import org.koin.test.AutoCloseKoinTest

class HealthRoutingTest : AutoCloseKoinTest() {

    private val testService = Service("serviceName", "port", "path")

    private val testProperties = YamlProperties(
        Logging(Level(LoggingLevel.INFO)),
        Schedule(true, 0),
        ClientServices(listOf(testService)),
        failureThreshold = 3,
        GeoHealthcheck("serviceName", "port")
    )

    private fun Application.configureTestDI() {
        val testModule = module {
            single { testProperties }
        }
        install(Koin) {
            SLF4JLogger()
            modules(testModule)
        }
    }

    private val applicationConfig: Application.() -> Unit = {
        configureRouting()
        configureSerialization()
        configureTestDI()
    }

    @Test
    fun `should answer OK to health`(): Unit = withTestApplication(applicationConfig) {
        handleRequest(HttpMethod.Get, "/health").apply {
            assertThat(response.status(), equalTo(HttpStatusCode.OK))
            assertThat(response.content, equalTo("OK"))
        }
    }

    @Test
    fun `should return map to hello`(): Unit = withTestApplication(applicationConfig) {
        handleRequest(HttpMethod.Get, "/hello").apply {
            assertNotNull(response.content)
            val result = Json.decodeFromString<Map<String, String>>(response.content!!)
            assertThat(result["a"], equalTo("b"))
        }
    }

    @Test
    fun `should return services`(): Unit = withTestApplication(applicationConfig) {
        handleRequest(HttpMethod.Get, "services").apply {
            println(response.content)
        }
    }
}