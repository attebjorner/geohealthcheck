package gosha.kalosha.di

import gosha.kalosha.properties.AppStatus
import gosha.kalosha.properties.PropertiesLoader
import gosha.kalosha.service.GeoHealthcheckMonitor
import gosha.kalosha.service.Scheduler
import gosha.kalosha.service.ServiceMonitor
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import mu.KotlinLogging
import org.koin.dsl.module
import org.koin.dsl.onClose

private val logger = KotlinLogging.logger {  }

fun mainModule(namespace: String, backwardCompatibility: Boolean) = module {
    single { PropertiesLoader(backwardCompatibility).load() }
    single { HttpClient(CIO) }
    single { AppStatus(namespace) }
    single { ServiceMonitor() }
    single { GeoHealthcheckMonitor() }
    single { Scheduler() }.onClose {
        logger.info { "Shutting down the scheduler" }
        it?.shutdown()
    }
}
