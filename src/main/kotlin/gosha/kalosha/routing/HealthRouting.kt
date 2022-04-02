package gosha.kalosha.routing

import gosha.kalosha.model.YamlProperties
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.utils.io.*
import org.koin.ktor.ext.inject

fun Routing.configureHealthRouts() {

    val properties by inject<YamlProperties>()

    get("health") {
        call.response.status(HttpStatusCode.OK)
        call.respondText("OK")
    }
    get("test") {
        call.respond(makeRequest())
    }
    get("hello") {
        call.respond(mapOf("a" to "b"))
    }
    get("services") {
        call.respond(properties.clientServices)
    }
}

suspend fun makeRequest(): String {
    val client = HttpClient(CIO)
    val response: HttpResponse = client.get("http://attebjorner.gq/")
    return if (response.status.isSuccess()) {
        response.content.readUTF8Line()!!.substring(0..10)
    } else {
        "error"
    }
}