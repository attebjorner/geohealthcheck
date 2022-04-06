package gosha.kalosha.properties

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppProperties(
    var logging: Logging,
    var schedule: Schedule,
    @SerialName("client-services")
    var clientServices: ClientServices,
    @SerialName("geo-healthcheck-list")
    var geoHealthcheckList: List<GeoHealthcheck> = listOf(),
)

@Serializable
data class Logging(
    var level: Level
)

@Serializable
data class Level(
    var root: LoggingLevel
)

@Serializable
enum class LoggingLevel {
    ERROR,
    WARN,
    INFO,
    DEBUG,
    TRACE;

    fun toSlf4jLevel(): org.slf4j.event.Level {
        return org.slf4j.event.Level.valueOf(this.name)
    }
}

@Serializable
data class Schedule(
    var enabled: Boolean,
    var delay: Long // in ms
)

@Serializable
data class ClientServices(
    @SerialName("service-list")
    var serviceSet: Set<Service> = setOf()
)

@Serializable
data class Service(
    @SerialName("service-name")
    var serviceName: String,
    var port: String,
    var path: String,
    @SerialName("failure-threshold")
    var failureThreshold: Int = 0,
    var delay: Long = 0
)

@Serializable
data class GeoHealthcheck(
    @SerialName("service-name")
    var serviceName: String,
    var port: String
)

data class AppStatus(
    val namespace: String,
    var isOk: Boolean = true,
    var geoHealthcheckIsOk: Boolean = true
)