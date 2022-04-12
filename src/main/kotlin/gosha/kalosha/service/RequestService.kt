package gosha.kalosha.service

import gosha.kalosha.properties.Service
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import mu.KotlinLogging

class RequestService(
    private val client: HttpClient
) {
    private val logger = KotlinLogging.logger {  }

    suspend fun updateStatus(service: Service) {
        if (!isStatusUp(service)) {
            ++service.timesFailed
        } else if (service.timesFailed >= service.failureThreshold) {
            service.timesFailed = 0
        }
    }

    private suspend fun isStatusUp(service: Service): Boolean {
        return try {
            logger.info { "Sending healthcheck to '${service.name}'" }
            val response: HttpResponse = client.get(service.endpoint)
            logger.info { "Got ${response.status.value} status code from '${service.name}'" }
            true
        } catch (ex: ResponseException) {
            logger.info { "Got ${ex.response.status.value} status code from '${service.name}'" }
            false
        } catch (ex: Exception) {
            logger.error { "${ex.message} from '${service.name}'" }
            false
        }
    }
}