package gosha.kalosha.properties

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import mu.KotlinLogging

private val logger = KotlinLogging.logger {  }

@Serializable
data class OldAppProperties(
    var logging: Logging,
    var schedule: Schedule,
    @SerialName("client-services")
    var clientServices: OldClientServices,
    @SerialName("failure-threshold")
    var failureThreshold: Int,
    @SerialName("geo-healthcheck")
    var geoHealthcheck: GeoHealthcheck
) {
    fun toProperties(): AppProperties {
        val serviceSet = clientServices.serviceList.toSet()
        if (serviceSet.size != clientServices.serviceList.size) {
            logger.warn { "serviceList contains duplicates" }
        }
        serviceSet.forEach {
            it.failureThreshold = failureThreshold
            it.delay = schedule.delay
        }
        return AppProperties(
            logging,
            schedule,
            ClientServices(serviceSet),
            listOf(geoHealthcheck)
        )
    }
}

@Serializable
data class OldClientServices(
    @SerialName("service-list")
    var serviceList: List<Service>
)

