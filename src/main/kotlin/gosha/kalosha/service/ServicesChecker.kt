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
    services: Collection<T>,
    private val url: (T) -> String,
    private val onSuccess: (T) -> Unit = {},
    private val onError: (T) -> Unit = {},
    private val check: () -> Boolean
) : KoinComponent {

    private val serviceCheckers = services.map { service ->
        SingleServiceChecker(service, url(service), { onSuccess(service) }, { onError(service) }) { false }
    }

    suspend fun isStatusUp(): Boolean {
        for (serviceChecker in serviceCheckers) {
            serviceChecker.isStatusUp()
        }
        return check()
    }
}

class SingleServiceChecker<T : Service>(
    private val service: T,
    private val url: String,
    private val onSuccess: () -> Unit = {},
    private val onError: () -> Unit = {},
    private val check: () -> Boolean
) : KoinComponent {

    private val logger = KotlinLogging.logger {  }

    private val client by inject<HttpClient>()

    suspend fun isStatusUp(): Boolean {
        try {
            logger.info { "Sending healthcheck to '${service.serviceName}'" }
            val response: HttpResponse = client.get(url)
            logger.info { "Got ${response.status.value} status code from '${service.serviceName}'" }
            onSuccess()
        } catch (ex: ResponseException) {
            logger.info { "Got ${ex.response.status.value} status code from '${service.serviceName}'" }
            onError()
        }
        return check()
    }
}