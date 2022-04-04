package gosha.kalosha.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient as SerialTransient

@Serializable
data class AppProperties(
    @SerialName("logging") var logging: Logging,
    @SerialName("schedule") var schedule: Schedule,
    @SerialName("client-services") var clientServices: ClientServices,
    @SerialName("failureThreshold") var failureThreshold: Int,
    @SerialName("geo-healthcheck") var geoHealthcheck: GeoHealthcheck
)

@Serializable
data class Logging(
    @SerialName("level") var level: Level
)

@Serializable
data class Level(
    @SerialName("root") var root: LoggingLevel
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
    @SerialName("enabled") var enabled: Boolean,
    @SerialName("delay") var delay: Long // in ms
)

@Serializable
data class ClientServices(
    @SerialName("serviceList") var serviceList: List<Service>
)

@Serializable
data class Service(
    @SerialName("serviceName") var serviceName: String,
    @SerialName("port") var port: String,
    @SerialName("path") var path: String,
    @SerialTransient var failureThreshold: Int = 0
)

@Serializable
data class GeoHealthcheck(
    @SerialName("serviceName") var serviceName: String,
    @SerialName("port") var port: String
)

data class AppStatus(
    val namespace: String,
    var isOk: Boolean = true
)