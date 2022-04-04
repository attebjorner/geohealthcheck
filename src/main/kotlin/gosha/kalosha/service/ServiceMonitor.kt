package gosha.kalosha.service

import gosha.kalosha.config.AppStatus
import gosha.kalosha.config.AppProperties
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.net.URL

private val logger = KotlinLogging.logger {  }

object ServiceMonitor : KoinComponent {

    private val client by inject<HttpClient>()

    private val properties by inject<AppProperties>()

    private val appStatus by inject<AppStatus>()

    private val services = properties.clientServices.serviceList

    private val failureThreshold = properties.failureThreshold

    private val repeatMillis = properties.schedule.delay

    init {
        services.forEach { it.failureThreshold = failureThreshold }
    }

    suspend fun monitor() {
        if (repeatMillis > 0) {
            while (true) {
                val areServicesUp = areServicesUp()
                if (!areServicesUp) {
                    appStatus.isOk = false
                    return
                }
                delay(repeatMillis)
            }
        } else {
            areServicesUp()
        }
    }

    private suspend fun areServicesUp(): Boolean {
        for (service in services) {
            try {
                logger.info { "Sending healthcheck to ${service.serviceName}" }
                val response: HttpResponse = client.get("http://${service.serviceName}:${service.port}${service.path}")
                logger.info { "Got ${response.status.value} status code" }
            } catch (ex: ResponseException) {
                logger.info { "Got ${ex.response.status.value} status code" }
                --service.failureThreshold
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        return services.all { it.failureThreshold != 0 }
    }
}