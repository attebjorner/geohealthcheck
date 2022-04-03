package gosha.kalosha.dto

import io.ktor.http.*
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class ResponseDto(
    val status: ResponseStatus,
    @Serializable(with = HttpStatusCodeSerializer::class)
    val code: HttpStatusCode,
    val namespace: String
)

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
