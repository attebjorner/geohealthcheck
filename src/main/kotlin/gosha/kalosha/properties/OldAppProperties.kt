package gosha.kalosha.properties

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OldAppProperties(
    val logging: Logging,
    val schedule: Schedule,
    @SerialName("client-services")
    val clientServices: ClientServices,
    @SerialName("failure-threshold")
    val failureThreshold: Int,
    @SerialName("geo-healthcheck")
    val geoHealthcheck: GeoHealthcheck
) {
    fun toProperties(): AppProperties {
        clientServices.apply {
            services = services.map {
                it.copy(failureThreshold = failureThreshold, delay = schedule.delay)
            }
        }
        return AppProperties(
            logging,
            schedule,
            clientServices,
            listOf(geoHealthcheck)
        )
    }
}
