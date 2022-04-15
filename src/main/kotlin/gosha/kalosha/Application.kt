package gosha.kalosha

import gosha.kalosha.config.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*

fun main(args: Array<String>) {
    val environment = commandLineEnvironment(args).apply {
        application.configure()
    }
    CIOApplicationEngine(environment) {
        loadCommonConfiguration(environment.config)
    }.apply {
        addShutdownHook { stop(3000, 5000) }
        start(wait = true)
    }
}
