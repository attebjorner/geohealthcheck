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

    suspend fun isStatusUp(service: Service): Boolean {
        return try {
            logger.info { "Sending healthcheck to '${service.endpoint}'" }
            val response: HttpResponse = client.get(service.endpoint)
            logger.info { "Got ${response.status.value} status code from '${service.endpoint}'" }
            true
        } catch (ex: ResponseException) {
            logger.info { "Got ${ex.response.status.value} status code from '${service.endpoint}'" }
            false
        } catch (ex: Exception) {
            logger.warn { "${ex.message} from '${service.endpoint}'" }
            false
        }
    }
}