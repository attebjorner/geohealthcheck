package gosha.kalosha.routing

import gosha.kalosha.config.*
import gosha.kalosha.properties.*
import io.ktor.http.*
import io.ktor.application.*
import io.ktor.server.testing.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.ktor.ext.Koin
import org.koin.logger.SLF4JLogger
import org.koin.test.junit5.AutoCloseKoinTest

const val TEST_NAMESPACE = "test_namespace"

class HealthRoutingTest : AutoCloseKoinTest() {

    private val appStatus = AppStatus(TEST_NAMESPACE)

    private val testService = Service("serviceName")

    private val testProperties = AppProperties(
        Logging(Level(LoggingLevel.INFO)),
        Schedule(true),
        ClientServices(setOf(testService)),
        listOf(Service("serviceName", 80))
    )

    private fun Application.configureTestDI() {
        val testModule = module {
            single { testProperties }
            single { appStatus }
        }
        install(Koin) {
            SLF4JLogger()
            modules(testModule)
        }
    }

    private val applicationConfig: Application.() -> Unit = {
        configureTestDI()
        configureRouting()
        configureSerialization()
        configureLogging()
    }

    @Test
    fun `should answer 200 to health when currentAppStatus is Ok`(): Unit = withTestApplication(applicationConfig) {
        appStatus.isOk.set(true)
        handleRequest(HttpMethod.Get, "/health").apply {
            assertThat(response.status(), equalTo(HttpStatusCode.OK))
            assertThat(response.content, equalTo("{\"status\":\"success\",\"code\":\"200\",\"namespace\":\"${TEST_NAMESPACE}\"}"))
        }
    }

    @Test
    fun `should answer 500 to health when currentAppStatus is not Ok`(): Unit = withTestApplication(applicationConfig) {
        appStatus.isOk.set(false)
        handleRequest(HttpMethod.Get, "/health").apply {
            assertThat(response.status(), equalTo(HttpStatusCode.InternalServerError))
            assertThat(response.content, equalTo("{\"status\":\"error\",\"code\":\"500\",\"namespace\":\"${TEST_NAMESPACE}\"}"))
        }
    }
}