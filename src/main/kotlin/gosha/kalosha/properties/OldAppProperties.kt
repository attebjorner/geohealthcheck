package gosha.kalosha.properties

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OldAppProperties(
    var logging: Logging,
    var schedule: Schedule,
    @SerialName("client-services")
    var clientServices: ClientServices,
    @SerialName("failure-threshold")
    var failureThreshold: Int,
    @SerialName("geo-healthcheck")
    var geoHealthcheck: GeoHealthcheck
) {
    fun toProperties(): AppProperties {
        clientServices.clientServices.forEach {
            it.failureThreshold = failureThreshold
            it.delay = schedule.delay
        }
        return AppProperties(
            logging,
            schedule,
            clientServices,
            listOf(geoHealthcheck)
        )
    }
}
