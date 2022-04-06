package gosha.kalosha.service

import gosha.kalosha.properties.AppStatus
import gosha.kalosha.properties.AppProperties
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.delay
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.atomic.AtomicBoolean

private val logger = KotlinLogging.logger {  }

object ServiceMonitor : KoinComponent {

    private val client by inject<HttpClient>()

    private val properties by inject<AppProperties>()

    private val appStatus by inject<AppStatus>()

    private val services = properties.clientServices.serviceSet

    private val delay = properties.schedule.delay

    private val isWorking = AtomicBoolean()

    suspend fun monitor() {
        while (true) {
            if (appStatus.isOk) {
                val areServicesUp = areServicesUp()
                appStatus.isOk = !appStatus.geoHealthcheckIsOk || areServicesUp
            }
            delay(delay)
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
                logger.error(ex.message)
            }
        }
        return services.all { it.failureThreshold != 0 }
    }
}