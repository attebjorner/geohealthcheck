package gosha.kalosha.service

import gosha.kalosha.properties.AppProperties
import gosha.kalosha.properties.AppStatus
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private val logger = KotlinLogging.logger {  }

class GeoHealthcheckMonitor : KoinComponent {

    private val client by inject<HttpClient>()

    private val properties by inject<AppProperties>()

    private val appStatus by inject<AppStatus>()

    //todo
    private val geoHealthcheck = properties.geoHealthcheckList[0]

    suspend fun checkGeoHealthcheckStatus() {
        try {
            logger.info { "Sending healthcheck to GeoHealthcheck ${geoHealthcheck.serviceName}" }
            val response: HttpResponse = client.get("http://${geoHealthcheck.serviceName}:${geoHealthcheck.port}/health")
            logger.info { "Got ${response.status.value} status code" }
            appStatus.geoHealthcheckIsOk.set(true)
        } catch (ex: ResponseException) {
            logger.info { "Got ${ex.response.status.value} status code" }
            appStatus.geoHealthcheckIsOk.set(false)
        } catch (ex: Exception) {
            logger.error(ex.message)
        }
    }
}