package gosha.kalosha.routing

import gosha.kalosha.properties.AppStatus
import gosha.kalosha.dto.ResponseDto
import gosha.kalosha.dto.ResponseStatus
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject

fun Routing.configureRouting() {

    val appStatus by inject<AppStatus>()

    get("health") {
        if (appStatus.isOk) {
            call.respond(
                HttpStatusCode.OK,
                ResponseDto(ResponseStatus.SUCCESS, HttpStatusCode.OK, appStatus.namespace)
            )
        } else {
            call.respond(
                HttpStatusCode.InternalServerError,
                ResponseDto(ResponseStatus.ERROR, HttpStatusCode.InternalServerError, appStatus.namespace)
            )
        }
    }
}
