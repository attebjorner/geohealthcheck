package gosha.kalosha.di

import gosha.kalosha.properties.AppProperties
import gosha.kalosha.entity.AppStatus
import gosha.kalosha.service.monitor.ServiceMonitor
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
            modules(mainModule(namespace, false, this::class.java.classLoader.getResource("application.yaml")!!.path))
        }

        val properties by inject<AppProperties>()
        val client by inject<HttpClient>()
        val appStatus by inject<AppStatus>()
        val serviceMonitor by inject<ServiceMonitor>()

        assertThat(properties, notNullValue())
        assertThat(client, notNullValue())
        assertThat(appStatus, notNullValue())
        assertThat(appStatus.namespace, equalTo(namespace))
        assertThat(serviceMonitor, notNullValue())
        stopKoin()
    }
}