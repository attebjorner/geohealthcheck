package gosha.kalosha.properties

import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OldAppProperties(
    val logging: Logging,
    val schedule: Schedule,
    @SerialName("client-services")
    val clientServices: OldClientServices,
    @SerialName("failure-threshold")
    val failureThreshold: Int,
    @SerialName("geo-healthcheck")
    val geoHealthcheck: OldGeoHealthcheck
) {
    fun toProperties(): AppProperties {
        val clientServices = clientServices.services.map {
            ClientService(
                endpoint = URLBuilder(
                    protocol = URLProtocol.HTTP,
                    host = it.serviceName,
                    port = it.port,
                    encodedPath = it.path
                ).buildString(),
                failureThreshold = failureThreshold,
                delay = schedule.delay
            )
        }.toSet()
        val geoHealthcheck = GeoHealthcheck(
            endpoint = URLBuilder(
                protocol = URLProtocol.HTTP,
                host = geoHealthcheck.serviceName,
                port = geoHealthcheck.port,
                encodedPath = "health"
            ).buildString(),
            failureThreshold = failureThreshold
        )
        return AppProperties(
            logging,
            schedule,
            ClientServices(clientServices),
            listOf(geoHealthcheck)
        )
    }
}

@Serializable
data class OldClientServices(
    @SerialName("service-list")
    var services: List<OldService>
)

@Serializable
data class OldService(
    @SerialName("service-name")
    val serviceName: String,
    val port: Int,
    val path: String,
)

@Serializable
data class OldGeoHealthcheck(
    @SerialName("service-name")
    val serviceName: String,
    val port: Int
)