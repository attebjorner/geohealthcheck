package gosha.kalosha.routing

import gosha.kalosha.config.YamlProperties
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.utils.io.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.ktor.ext.inject

fun Routing.configureHealthRouts() {

    val properties by inject<YamlProperties>()

    get("health") {
        call.response.status(HttpStatusCode.OK)
        call.respondText("OK")
    }
    get("hello") {
        call.respond(mapOf("a" to "b"))
    }
    get("services") {
        call.respond(properties.clientServices)
    }
    get("error1") {
        throw IllegalArgumentException("illegal argument")
    }
    get("error2") {
        throw IllegalStateException("illegal state")
    }
}
