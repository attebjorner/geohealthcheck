package gosha.kalosha.statuspage

import gosha.kalosha.dto.ResponseDto
import gosha.kalosha.dto.ResponseStatus
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*

fun StatusPages.Configuration.errorStatusPages(namespace: String) {
    exception<RuntimeException> { cause ->
        call.application.log.error(cause.message)
        call.respond(
            HttpStatusCode.InternalServerError,
            ResponseDto(ResponseStatus.ERROR, HttpStatusCode.InternalServerError, namespace)
        )
    }
}