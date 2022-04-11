package gosha.kalosha.di

import gosha.kalosha.properties.AppProperties
import gosha.kalosha.properties.AppStatus
import gosha.kalosha.service.monitor.ClientServiceMonitor
import gosha.kalosha.service.monitor.GeoHealthcheckMonitor
import gosha.kalosha.service.schedule.Scheduler
import io.ktor.client.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.junit5.AutoCloseKoinTest

internal class MainModuleKtTest : AutoCloseKoinTest() {

    @Test
    fun `should load modules`() {
        val namespace = "testnamespace"
        startKoin {
            modules(mainModule(namespace, false, "application.yaml"))
        }

        val properties by inject<AppProperties>()
        val client by inject<HttpClient>()
        val appStatus by inject<AppStatus>()
        val clientServiceMonitor by inject<ClientServiceMonitor>()
        val geoHealthcheckMonitor by inject<GeoHealthcheckMonitor>()
        val scheduler by inject<Scheduler>()

        assertThat(properties, notNullValue())
        assertThat(client, notNullValue())
        assertThat(appStatus, notNullValue())
        assertThat(appStatus.namespace, equalTo(namespace))
        assertThat(clientServiceMonitor, notNullValue())
        assertThat(geoHealthcheckMonitor, notNullValue())
        assertThat(scheduler, notNullValue())
        stopKoin()
    }
}