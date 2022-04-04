package gosha.kalosha

import gosha.kalosha.config.*
import gosha.kalosha.config.configureRouting
import io.ktor.application.*

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    configureDI()
    configureRouting()
    configureSerialization()
    configureLogging()
    scheduleJob()
}
