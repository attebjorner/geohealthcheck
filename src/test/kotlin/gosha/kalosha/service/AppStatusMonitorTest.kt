package gosha.kalosha.service

import gosha.kalosha.properties.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.KoinTestRule
import kotlin.test.Test

internal class AppStatusMonitorTest : AutoCloseKoinTest() {

    private val testProperties = AppProperties(
        Logging(Level(LoggingLevel.INFO)),
        Schedule(true, 10),
        ClientServices(setOf()),
        listOf()
    )

    private val appStatus = AppStatus("test")

    private val clientServiceMonitor: ClientServiceMonitor = mockk()

    private val geoHealthcheckMonitor: GeoHealthcheckMonitor = mockk()

    private val appStatusMonitor = AppStatusMonitor()

    private val trueFlow get() = flow { emit(true) }

    private val falseFlow get() = flow { emit(false) }

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(
            module {
                single { testProperties }
                single { appStatus }
                single { clientServiceMonitor }
                single { geoHealthcheckMonitor }
            }
        )
    }

    @Test
    fun `should set appStatus#isOk to true when geoHealthchecks not ok and services not ok`() = runBlocking {
        every { clientServiceMonitor.checkServices() } returns falseFlow
        every { geoHealthcheckMonitor.checkGeoHealthcheckStatus() } returns falseFlow
        appStatusMonitor.startMonitoring()
        verify { clientServiceMonitor.checkServices() }
        verify { geoHealthcheckMonitor.checkGeoHealthcheckStatus() }
        assertThat(appStatus.isOk.get(), equalTo(true))
    }

    @Test
    fun `should set appStatus#isOk to false when geoHealthchecks ok and services not ok`() = runBlocking {
        every { clientServiceMonitor.checkServices() } returns falseFlow
        every { geoHealthcheckMonitor.checkGeoHealthcheckStatus() } returns trueFlow
        appStatusMonitor.startMonitoring()
        verify { clientServiceMonitor.checkServices() }
        verify { geoHealthcheckMonitor.checkGeoHealthcheckStatus() }
        assertThat(appStatus.isOk.get(), equalTo(false))
    }

    @Test
    fun `should set appStatus#isOk to true when geoHealthchecks not ok and services ok`() = runBlocking {
        every { clientServiceMonitor.checkServices() } returns trueFlow
        every { geoHealthcheckMonitor.checkGeoHealthcheckStatus() } returns falseFlow
        appStatusMonitor.startMonitoring()
        verify { clientServiceMonitor.checkServices() }
        verify { geoHealthcheckMonitor.checkGeoHealthcheckStatus() }
        assertThat(appStatus.isOk.get(), equalTo(true))
    }

    @Test
    fun `should set appStatus#isOk to true when geoHealthchecks ok and services ok`() = runBlocking {
        every { clientServiceMonitor.checkServices() } returns trueFlow
        every { geoHealthcheckMonitor.checkGeoHealthcheckStatus() } returns trueFlow
        appStatusMonitor.startMonitoring()
        verify { clientServiceMonitor.checkServices() }
        verify { geoHealthcheckMonitor.checkGeoHealthcheckStatus() }
        assertThat(appStatus.isOk.get(), equalTo(true))
    }
}