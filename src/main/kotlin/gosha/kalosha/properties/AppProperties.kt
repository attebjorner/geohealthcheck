package gosha.kalosha.properties

import io.ktor.http.*
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
        val clientServiceSet = clientServices.services.toSet()
        if (clientServiceSet.size != clientServices.services.size) {
            throw RuntimeException("serviceList contains duplicates")
        }
        clientServices.services = clientServiceSet
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
    var services: Collection<ClientService> = setOf()
)

interface Service {
    val endpoint: String
    val failureThreshold: Int
    var timesFailed: Int
    val name: String

    val isUp: Boolean get() = timesFailed < failureThreshold
}

@Serializable
data class ClientService(
    override val endpoint: String,
    @SerialName("failure-threshold")
    override val failureThreshold: Int = 1,
    val delay: Long = 0,
    @Transient
    override var timesFailed: Int = 0
) : Service {
    override val name: String = Url(endpoint).host
}

@Serializable
data class GeoHealthcheck(
    override val endpoint: String,
    @SerialName("failure-threshold")
    override val failureThreshold: Int = 1,
    @Transient
    override var timesFailed: Int = 0
) : Service {
    override val name: String = Url(endpoint).host
}

data class AppStatus(
    val namespace: String,
    val isOk: AtomicBoolean = AtomicBoolean(true)
)
