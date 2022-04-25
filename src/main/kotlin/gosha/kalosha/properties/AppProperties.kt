package gosha.kalosha.properties

import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class AppProperties(
    @SerialName("logging")
    val logging: Logging,
    @SerialName("schedule")
    val schedule: Schedule,
    @SerialName("client-services")
    val clientServices: ClientServices,
    @SerialName("geo-healthcheck-list")
    var geoHealthchecks: Collection<Service> = setOf(),
) {
    init {
        clientServices.services = toSet(clientServices.services)
        geoHealthchecks = toSet(geoHealthchecks)
    }
}

private fun toSet(services: Collection<Service>): Set<Service> =
    services.toSet().also {
        if (it.size != services.size) {
            throw RuntimeException("Either service-list or geoHealthcheck-list contains duplicates")
        }
    }


@Serializable
data class Logging(
    @SerialName("level")
    val level: Level
)

@Serializable
data class Level(
    @SerialName("root")
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
    @SerialName("enabled")
    val enabled: Boolean
)

@Serializable
data class ClientServices(
    @SerialName("service-list")
    var services: Collection<Service> = setOf()
)

@Serializable
data class Service(
    @SerialName("endpoint")
    val endpoint: String,
    @SerialName("failure-threshold")
    val failureThreshold: Int = 1,
    @SerialName("delay")
    val delay: Long = 0
) {
    @Transient
    var timesFailed: Int = 0

    @Transient
    val name: String = Url(endpoint).host

    val isUp: Boolean get() = timesFailed < failureThreshold

    fun resetFails() {
        timesFailed = 0
    }

    fun countFail() {
        if (isUp) {
            ++timesFailed
        }
    }
}
