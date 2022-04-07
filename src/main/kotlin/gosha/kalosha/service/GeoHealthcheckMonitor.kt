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

class GeoHealthcheckMonitor : KoinComponent {

    private val logger = KotlinLogging.logger {  }

    private val client by inject<HttpClient>()

    private val properties by inject<AppProperties>()

    private val appStatus by inject<AppStatus>()

    private val geoHealthchecks = properties.geoHealthcheckList

    suspend fun checkGeoHealthcheckStatus() {
        val areGeoHealthchecksUp = areGeoHealthchecksUp()
        appStatus.geoHealthcheckIsOk.set(areGeoHealthchecksUp)
    }

    private suspend fun areGeoHealthchecksUp(): Boolean {
        for (geoHealthcheck in geoHealthchecks) {
            try {
                logger.info { "Sending healthcheck to GeoHealthcheck '${geoHealthcheck.serviceName}'" }
                val response: HttpResponse = client.get("http://${geoHealthcheck.serviceName}:${geoHealthcheck.port}/health")
                logger.info { "Got ${response.status.value} status code from GeoHealthcheck '${geoHealthcheck.serviceName}'" }
                geoHealthcheck.isOk = true
            } catch (ex: ResponseException) {
                logger.info { "Got ${ex.response.status.value} status code GeoHealthcheck '${geoHealthcheck.serviceName}'" }
                geoHealthcheck.isOk = false
            } catch (ex: Exception) {
                logger.error(ex.message)
            }
        }
        return geoHealthchecks.any { it.isOk }
    }
}
