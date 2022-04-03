package gosha.kalosha.config

import gosha.kalosha.di.mainModule
import gosha.kalosha.service.Scheduler
import gosha.kalosha.service.ServiceMonitor.askServices
import gosha.kalosha.statuspage.errorStatusPages
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.features.*
import io.ktor.request.*
import io.ktor.serialization.*
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject
import org.koin.logger.SLF4JLogger

const val POD_NAMESPACE_PROPERTY = "pod.namespace"

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}

fun Application.configureDI() {
    install(Koin) {
        SLF4JLogger()
        modules(mainModule(coroutineContext))
    }
}

fun Application.configureLogging() {
    val properties by inject<YamlProperties>()

    install(CallLogging) {
        level = properties.logging.level.root.toSlf4jLevel()
        filter { call -> call.request.path().startsWith("/") }
    }
}
fun Application.scheduleJob() {
    val properties by inject<YamlProperties>()
    val schedule = properties.schedule

    if (schedule.enabled) {
        Scheduler.schedule(schedule.delay) {
            log.info("some job")
            askServices()
        }
    }
}
fun Application.configureStatusPages() {
    val namespace = environment.config.property(POD_NAMESPACE_PROPERTY).getString()

    install(StatusPages) {
        errorStatusPages(namespace)
    }
}