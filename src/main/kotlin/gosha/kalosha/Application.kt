package gosha.kalosha

import gosha.kalosha.config.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*

fun main(args: Array<String>) {
    embeddedServer(
        factory = CIO,
        environment = commandLineEnvironment(args).apply {
            application.configure()
        }
    ).start(wait = true)
}
