package gosha.kalosha.service.monitor

import gosha.kalosha.properties.*
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

    private val serviceMonitor: ServiceMonitor = mockk()

    @InjectMockKs
    private lateinit var appStatusMonitor: AppStatusMonitor

    private val trueFlow get() = flow { emit(true) }

    private val falseFlow get() = flow { emit(false) }

    @Test
    fun `should set appStatus#isOk to true when geoHealthchecks not ok and services not ok`() = runBlocking {
        every { serviceMonitor.checkClientServices() } returns falseFlow
        every { serviceMonitor.checkGeoHealthchecks() } returns falseFlow
        appStatusMonitor.startMonitoring()
        verify { serviceMonitor.checkClientServices() }
        verify { serviceMonitor.checkGeoHealthchecks() }
        assertThat(appStatus.isOk.get(), equalTo(true))
    }

    @Test
    fun `should set appStatus#isOk to false when geoHealthchecks ok and services not ok`() = runBlocking {
        every { serviceMonitor.checkClientServices() } returns falseFlow
        every { serviceMonitor.checkGeoHealthchecks() } returns trueFlow
        appStatusMonitor.startMonitoring()
        verify { serviceMonitor.checkClientServices() }
        verify { serviceMonitor.checkGeoHealthchecks() }
        assertThat(appStatus.isOk.get(), equalTo(false))
    }

    @Test
    fun `should set appStatus#isOk to true when geoHealthchecks not ok and services ok`() = runBlocking {
        every { serviceMonitor.checkClientServices() } returns trueFlow
        every { serviceMonitor.checkGeoHealthchecks() } returns falseFlow
        appStatusMonitor.startMonitoring()
        verify { serviceMonitor.checkClientServices() }
        verify { serviceMonitor.checkGeoHealthchecks() }
        assertThat(appStatus.isOk.get(), equalTo(true))
    }

    @Test
    fun `should set appStatus#isOk to true when geoHealthchecks ok and services ok`() = runBlocking {
        every { serviceMonitor.checkClientServices() } returns trueFlow
        every { serviceMonitor.checkGeoHealthchecks() } returns trueFlow
        appStatusMonitor.startMonitoring()
        verify { serviceMonitor.checkClientServices() }
        verify { serviceMonitor.checkGeoHealthchecks() }
        assertThat(appStatus.isOk.get(), equalTo(true))
    }
}