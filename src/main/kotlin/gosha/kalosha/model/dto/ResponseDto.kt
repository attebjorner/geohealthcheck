package gosha.kalosha.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseDto(
    val status: ResponseStatus,
    val code: String,
    val namespace: String
)

@Serializable
enum class ResponseStatus {
    @SerialName("success") SUCCESS,
    @SerialName("error") ERROR
}