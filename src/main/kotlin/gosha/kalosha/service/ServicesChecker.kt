package gosha.kalosha.service

import gosha.kalosha.properties.Service
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ServicesChecker<T : Service>(
    val services: Collection<T>,
    val url: (T) -> String,
    val onSuccess: (T) -> Unit = {},
    val onError: (T) -> Unit = {},
    val check: () -> Boolean
) : KoinComponent {

    private val logger = KotlinLogging.logger {  }

    private val client by inject<HttpClient>()

    suspend fun isStatusUp(): Boolean {
        for (service in services) {
            try {
                logger.info { "Sending healthcheck to '${service.serviceName}'" }
                val response: HttpResponse = client.get(url(service))
                logger.info { "Got ${response.status.value} status code from '${service.serviceName}'" }
                onSuccess(service)
            } catch (ex: ResponseException) {
                logger.info { "Got ${ex.response.status.value} status code from '${service.serviceName}'" }
                onError(service)
            } catch (ex: Exception) {
                logger.error(ex.message)
            }
        }
        return check()
    }
}
