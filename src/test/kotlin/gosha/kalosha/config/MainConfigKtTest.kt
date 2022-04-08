package gosha.kalosha.config

import gosha.kalosha.service.AppStatusMonitor
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Rule
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.KoinTestRule
import kotlin.test.Test

internal class MainConfigKtTest : AutoCloseKoinTest() {

    private val appStatusMonitor: AppStatusMonitor = mockk()

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(
            module {
                single { appStatusMonitor }
            }
        )
    }

    @Test
    fun `should start app monitoring`() = withTestApplication {
        coEvery { appStatusMonitor.startMonitoring() } returns Unit
        application.scheduleMonitoring()
        coVerify { appStatusMonitor.startMonitoring() }
    }
}