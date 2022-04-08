package gosha.kalosha.properties

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.concurrent.atomic.AtomicBoolean

@Serializable
data class AppProperties(
    val logging: Logging,
    val schedule: Schedule,
    @SerialName("client-services")
    val clientServices: ClientServices,
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
    val level: Level
)

@Serializable
data class Level(
    val root: LoggingLevel
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
    val enabled: Boolean,
    val delay: Long // in ms
)

@Serializable
data class ClientServices(
    @SerialName("service-list")
    var clientServices: Collection<ClientService> = setOf()
)

interface Service {
    val serviceName: String
}

@Serializable
data class ClientService(
    @SerialName("service-name")
    override val serviceName: String,
    val port: String,
    val path: String,
    @SerialName("failure-threshold")
    val failureThreshold: Int = 0,
    val delay: Long = 0,
    @Transient
    var timesFailed: Int = 0
) : Service

@Serializable
data class GeoHealthcheck(
    @SerialName("service-name")
    override val serviceName: String,
    val port: String,
    @Transient
    var isOk: Boolean = true
) : Service

data class AppStatus(
    val namespace: String,
    val isOk: AtomicBoolean = AtomicBoolean(true)
)
