package gosha.kalosha.config

import gosha.kalosha.di.mainModule
import gosha.kalosha.service.ServiceMonitor.monitor
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.request.*
import io.ktor.serialization.*
import kotlinx.coroutines.launch
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject
import org.koin.logger.SLF4JLogger

const val POD_NAMESPACE_PROPERTY = "healthcheck.pod.namespace"

const val INTEROPERABILITY_PROPERTY = "healthcheck.interoperability"

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}

fun Application.configureDI() {
    val namespace = environment.config.property(POD_NAMESPACE_PROPERTY).getString()

    install(Koin) {
        SLF4JLogger()
        modules(mainModule(namespace))
    }
}

fun Application.configureLogging() {
    val properties by inject<AppProperties>()

    install(CallLogging) {
        level = properties.logging.level.root.toSlf4jLevel()
        filter { call -> call.request.path().startsWith("/") }
    }
}

fun Application.scheduleJob() {
    val properties by inject<AppProperties>()
    if (properties.schedule.enabled) {
        launch {
            monitor()
        }
    }
}
