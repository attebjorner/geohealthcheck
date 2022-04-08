package gosha.kalosha.properties

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.concurrent.atomic.AtomicBoolean

@Serializable
data class AppProperties(
    var logging: Logging,
    var schedule: Schedule,
    @SerialName("client-services")
    var clientServices: ClientServices,
    @SerialName("geo-healthcheck-list")
    var geoHealthchecks: Collection<GeoHealthcheck> = setOf(),
) {
    init {
        val clientServiceSet = clientServices.clientServices.toSet()
        if (clientServiceSet.size != clientServices.clientServices.size) {
            throw RuntimeException("serviceList contains duplicates")
        }
        clientServices.clientServices = clientServiceSet
        val geoHealthcheckSet = geoHealthchecks.toSet()
        if (geoHealthcheckSet.size != geoHealthchecks.size) {
            throw RuntimeException("geoHealthcheckList contains duplicates")
        }
        geoHealthchecks = geoHealthcheckSet
    }
}

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
    var clientServices: Collection<ClientService> = setOf()
)

interface Service {
    var serviceName: String
}

@Serializable
data class ClientService(
    @SerialName("service-name")
    override var serviceName: String,
    var port: String,
    var path: String,
    @SerialName("failure-threshold")
    var failureThreshold: Int = 0,
    var delay: Long = 0
) : Service

@Serializable
data class GeoHealthcheck(
    @SerialName("service-name")
    override var serviceName: String,
    var port: String,
    var isOk: Boolean = true
) : Service

data class AppStatus(
    val namespace: String,
    val isOk: AtomicBoolean = AtomicBoolean(true)
)
