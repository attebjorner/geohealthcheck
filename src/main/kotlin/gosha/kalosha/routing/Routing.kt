package gosha.kalosha.routing

import gosha.kalosha.dto.ResponseDto
import gosha.kalosha.dto.ResponseStatus
import gosha.kalosha.entity.AppStatus
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject

fun Routing.configureRouting() {

    val appStatus by inject<AppStatus>()

    val successResponse = ResponseDto(ResponseStatus.SUCCESS, HttpStatusCode.OK, appStatus.namespace)

    val errorResponse = ResponseDto(ResponseStatus.ERROR, HttpStatusCode.InternalServerError, appStatus.namespace)

    get("health") {
        if (appStatus.isOk.get()) {
            call.respondText(successResponse.toJson(), ContentType.Application.Json, HttpStatusCode.OK)
        } else {
            call.respondText(errorResponse.toJson(), ContentType.Application.Json, HttpStatusCode.InternalServerError)
        }
    }
}
