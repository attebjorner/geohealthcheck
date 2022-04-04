package gosha.kalosha.config

import gosha.kalosha.routing.configureRouting
import io.ktor.application.*
import io.ktor.routing.*

fun Application.configureRouting() {

    routing {
        configureRouting()
    }
}
