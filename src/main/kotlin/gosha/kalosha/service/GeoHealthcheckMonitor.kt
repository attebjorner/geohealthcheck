package gosha.kalosha.service

import gosha.kalosha.properties.AppProperties
import gosha.kalosha.service.schedule.Scheduler
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.flow.flow
import mu.KotlinLogging

const val GEOHEALTHCHECKS_TASK = "geohealthchecks_status"

class GeoHealthcheckMonitor(
    properties: AppProperties,
    private val scheduler: Scheduler,
    private val client: HttpClient
) {

    private val logger = KotlinLogging.logger {  }

    private val delay = properties.schedule.delay

    private val geoHealthchecks = properties.geoHealthchecks

    fun checkGeoHealthcheckStatus() = flow {
        scheduler.createTask(GEOHEALTHCHECKS_TASK, delay) {
            emit(isStatusUp())
        }.start()
    }

    private suspend fun isStatusUp(): Boolean {
        for (geoHealthcheck in geoHealthchecks) {
            try {
                logger.info { "Sending healthcheck to '${geoHealthcheck.serviceName}'" }
                val response: HttpResponse = client.get(geoHealthcheck.url)
                logger.info { "Got ${response.status.value} status code from '${geoHealthcheck.serviceName}'" }
                geoHealthcheck.isOk = true
            } catch (ex: ResponseException) {
                logger.info { "Got ${ex.response.status.value} status code from '${geoHealthcheck.serviceName}'" }
                geoHealthcheck.isOk = false
            }
        }
        return geoHealthchecks.any { it.isOk }
    }
}
