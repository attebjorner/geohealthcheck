package gosha.kalosha.service.monitor

import gosha.kalosha.properties.AppStatus
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class AppStatusMonitorTest {

    private val appStatus = spyk(AppStatus("test"))

    private val clientServiceMonitor: ClientServiceMonitor = mockk()

    private val geoHealthcheckMonitor: GeoHealthcheckMonitor = mockk()

    @InjectMockKs
    private lateinit var appStatusMonitor: AppStatusMonitor

    private val trueFlow get() = flow { emit(true) }

    private val falseFlow get() = flow { emit(false) }

    @Test
    fun `should set appStatus#isOk to true when geoHealthchecks not ok and services not ok`() = runBlocking {
        every { clientServiceMonitor.checkServices() } returns falseFlow
        coEvery { geoHealthcheckMonitor.areGeoHealthchecksUp() } returns false
        appStatusMonitor.startMonitoring()
        verify { clientServiceMonitor.checkServices() }
        coVerify { geoHealthcheckMonitor.areGeoHealthchecksUp() }
        assertThat(appStatus.isOk.get(), equalTo(true))
    }

    @Test
    fun `should set appStatus#isOk to false when geoHealthchecks ok and services not ok`() = runBlocking {
        every { clientServiceMonitor.checkServices() } returns falseFlow
        coEvery { geoHealthcheckMonitor.areGeoHealthchecksUp() } returns true
        appStatusMonitor.startMonitoring()
        verify { clientServiceMonitor.checkServices() }
        coVerify { geoHealthcheckMonitor.areGeoHealthchecksUp() }
        assertThat(appStatus.isOk.get(), equalTo(false))
    }

    @Test
    fun `should not call geohealthcheck when services ok`() = runBlocking {
        every { clientServiceMonitor.checkServices() } returns trueFlow
        coEvery { geoHealthcheckMonitor.areGeoHealthchecksUp() } returns false
        appStatusMonitor.startMonitoring()
        verify { clientServiceMonitor.checkServices() }
        coVerify(exactly = 0) { geoHealthcheckMonitor.areGeoHealthchecksUp() }
        assertThat(appStatus.isOk.get(), equalTo(true))
    }
}