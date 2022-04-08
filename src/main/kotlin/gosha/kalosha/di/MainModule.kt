package gosha.kalosha.di

import gosha.kalosha.properties.AppStatus
import gosha.kalosha.properties.PropertiesLoader
import gosha.kalosha.service.AppStatusMonitor
import gosha.kalosha.service.ClientServiceMonitor
import gosha.kalosha.service.GeoHealthcheckMonitor
import gosha.kalosha.service.schedule.Scheduler
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import org.koin.dsl.module
import org.koin.dsl.onClose

fun mainModule(
    namespace: String,
    backwardCompatibility: Boolean
) = module {
    single { PropertiesLoader(backwardCompatibility).load() }
    single { HttpClient(CIO) }
    single { AppStatus(namespace) }
    single { ClientServiceMonitor() }
    single { GeoHealthcheckMonitor() }
    single { Scheduler() }.onClose { it?.shutdownAll() }
    single { AppStatusMonitor() }
}
