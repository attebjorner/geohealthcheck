package gosha.kalosha.config

import gosha.kalosha.di.mainModule
import gosha.kalosha.properties.AppProperties
import gosha.kalosha.routing.configureRouting
import gosha.kalosha.service.GeoHealthcheckMonitor
import gosha.kalosha.service.schedule.Scheduler
import gosha.kalosha.service.ServiceMonitor
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.serialization.*
import kotlinx.coroutines.launch
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject
import org.koin.logger.SLF4JLogger

const val POD_NAMESPACE_PROPERTY = "healthcheck.pod.namespace"

const val BACKWARD_COMPATIBILITY_PROPERTY = "healthcheck.backward-compatibility"

fun Application.configureRouting() {
    routing {
        configureRouting()
    }
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}

fun Application.configureDI() {
    val namespace = environment.config.propertyOrNull(POD_NAMESPACE_PROPERTY)?.getString() ?: ""
    val backwardCompatibility = environment.config.propertyOrNull(BACKWARD_COMPATIBILITY_PROPERTY)?.getString().toBoolean()

    install(Koin) {
        SLF4JLogger()
        modules(mainModule(namespace, backwardCompatibility))
    }
}

fun Application.configureLogging() {
    val properties by inject<AppProperties>()

    install(CallLogging) {
        level = properties.logging.level.root.toSlf4jLevel()
        filter { call -> call.request.path().startsWith("/") }
    }
}

fun Application.scheduleMonitorJobs() {
    val properties by inject<AppProperties>()
    val serviceMonitor by inject<ServiceMonitor>()
    val geoHealthcheckMonitor by inject<GeoHealthcheckMonitor>()
    val scheduler by inject<Scheduler>()
    val delay = properties.schedule.delay
    if (properties.schedule.enabled) {
        launch {
            scheduler.createTask("services status", delay) {
                serviceMonitor.checkServices()
            }.schedule()
        }
        launch {
            scheduler.createTask("geoHealthcheck status", delay) {
                geoHealthcheckMonitor.checkGeoHealthcheckStatus()
            }.schedule()
        }
    }
}
