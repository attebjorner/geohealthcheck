package gosha.kalosha.di

import gosha.kalosha.entity.AppStatus
import gosha.kalosha.properties.PropertiesLoader
import gosha.kalosha.service.RequestService
import gosha.kalosha.service.monitor.AppStatusMonitor
import gosha.kalosha.service.monitor.ServiceMonitor
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import org.koin.dsl.module

fun mainModule(
    namespace: String,
    backwardCompatibility: Boolean,
    configName: String?
) = module {
    single { PropertiesLoader(backwardCompatibility, configName).load() }
    single { HttpClient(CIO) }
    single { AppStatus(namespace) }
    single { RequestService(get()) }
    single { ServiceMonitor(get()) }
    single { AppStatusMonitor(get(), get(), get()) }
}
