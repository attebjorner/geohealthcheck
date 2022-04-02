package gosha.kalosha.routing

import gosha.kalosha.model.YamlProperties
import io.ktor.application.*
import io.ktor.routing.*

fun Application.configureRouting() {

    routing {
        configureHealthRouts()
    }
}
