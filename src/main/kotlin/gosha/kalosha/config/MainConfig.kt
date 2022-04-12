package gosha.kalosha.config

import gosha.kalosha.di.mainModule
import gosha.kalosha.properties.AppProperties
import gosha.kalosha.routing.configureRouting
import gosha.kalosha.service.monitor.AppStatusMonitor
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.serialization.*
import kotlinx.coroutines.launch
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject
import org.koin.logger.SLF4JLogger

private const val POD_NAMESPACE_PROPERTY = "healthcheck.pod.namespace"

private const val BACKWARD_COMPATIBILITY_PROPERTY = "healthcheck.backward-compatibility"

private const val CONFIG_PROPERTY = "healthcheck.config"

fun Application.configure() {
    configureDI()
    configureRouting()
    configureSerialization()
    configureLogging()
    scheduleMonitoring()
}

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
    val configName = environment.config.propertyOrNull(CONFIG_PROPERTY)?.getString()
    log.info(configName)

    install(Koin) {
        SLF4JLogger()
        modules(mainModule(namespace, backwardCompatibility, configName))
    }
}

fun Application.configureLogging() {
    val properties by inject<AppProperties>()

    install(CallLogging) {
        level = properties.logging.level.root.toSlf4jLevel()
        filter { call -> call.request.path().startsWith("/") }
    }
}

fun Application.scheduleMonitoring() {
    val properties by inject<AppProperties>()
    if (!properties.schedule.enabled) {
        return
    }
    val appStatusMonitor by inject<AppStatusMonitor>()
    launch { appStatusMonitor.startMonitoring() }
}
