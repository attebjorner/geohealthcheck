package gosha.kalosha.dto

import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

internal class ResponseDtoTest {

    @Test
    fun `should serialize`() {
        val successDto = ResponseDto(ResponseStatus.SUCCESS, HttpStatusCode.OK, "namespace")
        val errorDto = ResponseDto(ResponseStatus.ERROR, HttpStatusCode.InternalServerError, "namespace")
        val successResponse = "{\"status\":\"success\",\"code\":\"200\",\"namespace\":\"namespace\"}"
        val errorResponse = "{\"status\":\"error\",\"code\":\"500\",\"namespace\":\"namespace\"}"
        assertThat(Json.encodeToString(successDto), equalTo(successResponse))
        assertThat(Json.encodeToString(errorDto), equalTo(errorResponse))
    }
}