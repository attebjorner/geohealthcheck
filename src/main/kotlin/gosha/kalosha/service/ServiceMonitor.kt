package gosha.kalosha.service

import gosha.kalosha.properties.AppProperties
import gosha.kalosha.properties.AppStatus
import gosha.kalosha.properties.Service
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.delay
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.atomic.AtomicBoolean

class ServiceMonitor : KoinComponent {

    private val logger = KotlinLogging.logger {  }

    private val client by inject<HttpClient>()

    private val properties by inject<AppProperties>()

    private val appStatus by inject<AppStatus>()

    private val services = properties.clientServices.serviceSet

    suspend fun checkServices() {
        val areServicesUp = areServicesUp()
        appStatus.isOk.set(!appStatus.geoHealthcheckIsOk.get() || areServicesUp)
    }

    private suspend fun areServicesUp(): Boolean {
        for (service in services) {
            try {
                logger.info { "Sending healthcheck to '${service.serviceName}'" }
                val response: HttpResponse = client.get("http://${service.serviceName}:${service.port}${service.path}")
                logger.info { "Got ${response.status.value} status code from '${service.serviceName}" }
            } catch (ex: ResponseException) {
                logger.info { "Got ${ex.response.status.value} status code from '${service.serviceName}" }
                --service.failureThreshold
            } catch (ex: Exception) {
                logger.error(ex.message)
            }
        }
        return services.all { it.failureThreshold != 0 }
    }
}