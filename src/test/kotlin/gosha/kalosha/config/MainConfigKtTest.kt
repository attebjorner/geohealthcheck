package gosha.kalosha.config

import gosha.kalosha.properties.*
import gosha.kalosha.service.GeoHealthcheckMonitor
import gosha.kalosha.service.ServiceMonitor
import gosha.kalosha.service.schedule.Scheduler
import io.ktor.server.testing.*
import io.mockk.*
import org.junit.Before
import org.junit.Rule
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.KoinTestRule
import kotlin.test.Test

internal class MainConfigKtTest : AutoCloseKoinTest() {

    private val testProperties = AppProperties(
        Logging(Level(LoggingLevel.INFO)),
        Schedule(true, 100),
        ClientServices(setOf()),
        listOf(GeoHealthcheck("serviceName", "port"))
    )

    private val serviceMonitor: ServiceMonitor = mockk()

    private val geoHealthcheckMonitor: GeoHealthcheckMonitor = mockk()

    private val scheduler = spyk(Scheduler)

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(
            module {
                single { testProperties }
                single { serviceMonitor }
                single { geoHealthcheckMonitor }
                single { scheduler }
            }
        )
    }

    @Before
    fun setUp() {
        coEvery { serviceMonitor.checkServices() } returns Unit
        coEvery { geoHealthcheckMonitor.checkGeoHealthcheckStatus() } returns Unit
    }

    @Test
    fun `should schedule jobs`() = withTestApplication {
        application.scheduleMonitorJobs()
        verify(exactly = 2) { scheduler.createTask(any(), any(), any()) }
        coVerify { serviceMonitor.checkServices() }
        coVerify { geoHealthcheckMonitor.checkGeoHealthcheckStatus() }
    }
}