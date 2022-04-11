package gosha.kalosha.config

import gosha.kalosha.properties.*
import gosha.kalosha.service.monitor.AppStatusMonitor
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.junit5.AutoCloseKoinTest

internal class MainConfigKtTest : AutoCloseKoinTest() {

    private val appStatusMonitor: AppStatusMonitor = mockk()

    private fun startKoinWithSchedulingSetTo(enabled: Boolean) {
        startKoin {
            modules(
                module {
                    single { AppProperties(
                        Logging(Level(LoggingLevel.INFO)),
                        Schedule(enabled, 1),
                        ClientServices(setOf()),
                        listOf()
                    ) }
                    single { appStatusMonitor }
                }
            )
        }
    }

    @BeforeEach
    fun setUp() {
        stopKoin()
    }

    @Test
    fun `should start app monitoring`() = withTestApplication {
        startKoinWithSchedulingSetTo(true)
        coEvery { appStatusMonitor.startMonitoring() } returns Unit
        application.scheduleMonitoring()
        coVerify { appStatusMonitor.startMonitoring() }
    }

    @Test
    fun `should not start app monitoring when scheduling is disabled`() = withTestApplication {
        startKoinWithSchedulingSetTo(false)
        application.scheduleMonitoring()
        coVerify(exactly = 0) { appStatusMonitor.startMonitoring() }
    }
}