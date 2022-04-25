package gosha.kalosha.dto

import io.ktor.http.*
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

@Serializable
data class ResponseDto(
    @SerialName("status")
    val status: ResponseStatus,
    @Serializable(with = HttpStatusCodeSerializer::class)
    @SerialName("code")
    val code: HttpStatusCode,
    @SerialName("namespace")
    val namespace: String
) {
    // для грааля
    fun toJson(): String =
        Json.encodeToString(serializer(), this)
}

@Serializable
enum class ResponseStatus {
    @SerialName("success") SUCCESS,
    @SerialName("error") ERROR
}

class HttpStatusCodeSerializer : KSerializer<HttpStatusCode> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("code", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): HttpStatusCode {
        return HttpStatusCode.fromValue(decoder.decodeString().toInt())
    }

    override fun serialize(encoder: Encoder, value: HttpStatusCode) {
        encoder.encodeString(value.value.toString())
    }
}
