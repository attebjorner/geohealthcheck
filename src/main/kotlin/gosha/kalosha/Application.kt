package gosha.kalosha

import org.slf4j.event.Level
import gosha.kalosha.di.mainModule
import gosha.kalosha.model.YamlProperties
import org.koin.ktor.ext.Koin
import io.ktor.application.*
import io.ktor.features.*
import org.koin.logger.SLF4JLogger
import gosha.kalosha.routing.*
import io.ktor.request.*
import io.ktor.serialization.*
import org.koin.ktor.ext.inject

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    configureDI()
    configureRouting()
    configureSerialization()
    configureLogging()
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}

fun Application.configureDI() {
    install(Koin) {
        SLF4JLogger()
        modules(mainModule)
    }
}

fun Application.configureLogging() {
    val properties by inject<YamlProperties>()

    install(CallLogging) {
        level = properties.logging.level.root.toSlf4jLevel()
        filter { call -> call.request.path().startsWith("/") }
    }
}
