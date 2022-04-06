package gosha.kalosha.routing

import gosha.kalosha.config.*
import gosha.kalosha.properties.*
import io.ktor.http.*
import io.ktor.application.*
import kotlin.test.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.spyk
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.koin.dsl.module
import org.koin.ktor.ext.Koin
import org.koin.logger.SLF4JLogger
import org.koin.test.AutoCloseKoinTest

const val TEST_NAMESPACE = "test_namespace"

class HealthRoutingTest : AutoCloseKoinTest() {

    private val appStatus = spyk(AppStatus(TEST_NAMESPACE))

    private val testService = Service("serviceName", "port", "path")

    private val testProperties = AppProperties(
        Logging(Level(LoggingLevel.INFO)),
        Schedule(true, 0),
        ClientServices(listOf(testService)),
        failureThreshold = 3,
        listOf(GeoHealthcheck("serviceName", "port"))
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
        configureRouting()
        configureSerialization()
        configureTestDI()
    }

    @Test
    fun `should answer 200 to health when currentAppStatus is Ok`(): Unit = withTestApplication(applicationConfig) {
        every { appStatus.isOk } returns true
        handleRequest(HttpMethod.Get, "/health").apply {
            assertThat(response.status(), equalTo(HttpStatusCode.OK))
            assertThat(response.content, equalTo("{\"status\":\"success\",\"code\":\"200\",\"namespace\":\"${TEST_NAMESPACE}\"}"))
        }
    }

    @Test
    fun `should answer 500 to health when currentAppStatus is not Ok`(): Unit = withTestApplication(applicationConfig) {
        every { appStatus.isOk } returns false
        handleRequest(HttpMethod.Get, "/health").apply {
            assertThat(response.status(), equalTo(HttpStatusCode.InternalServerError))
            assertThat(response.content, equalTo("{\"status\":\"error\",\"code\":\"500\",\"namespace\":\"${TEST_NAMESPACE}\"}"))
        }
    }
}