package gosha.kalosha.service.monitor

import gosha.kalosha.properties.AppStatus
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import mu.KotlinLogging

class AppStatusMonitor(
    private val appStatus: AppStatus,
    private val clientServiceMonitor: ClientServiceMonitor,
    private val geoHealthcheckMonitor: GeoHealthcheckMonitor
) {
    private val logger = KotlinLogging.logger {  }

    suspend fun startMonitoring() {
        val clientServiceFlow = clientServiceMonitor.checkServices()
        val geoHealthcheckFlow = geoHealthcheckMonitor.checkGeoHealthcheckStatus()
        combine(clientServiceFlow, geoHealthcheckFlow) { areServicesOk, areGeoHealthchecksOk ->
            areServicesOk to areGeoHealthchecksOk
        }.collectLatest { (areServicesOk, areGeoHealthchecksOk) ->
            logger.debug { "collecting $areServicesOk $areGeoHealthchecksOk" } //todo remove this
            appStatus.isOk.set(areServicesOk || !areGeoHealthchecksOk)
        }
    }
}
