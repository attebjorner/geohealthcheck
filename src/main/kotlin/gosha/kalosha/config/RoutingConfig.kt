package gosha.kalosha.config

import gosha.kalosha.routing.configureHealthRouts
import io.ktor.application.*
import io.ktor.routing.*

fun Application.configureRouting() {

    routing {
        configureHealthRouts()
    }
}
