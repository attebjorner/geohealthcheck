package gosha.kalosha.service.monitor

import gosha.kalosha.entity.AppStatus
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import mu.KotlinLogging

class AppStatusMonitor(
    private val appStatus: AppStatus,
    private val serviceMonitor: ServiceMonitor,
) {
    private val logger = KotlinLogging.logger {  }

    suspend fun startMonitoring() {
        val clientServiceFlow = serviceMonitor.checkClientServices()
        val geoHealthchecksFlow = serviceMonitor.checkGeoHealthchecks()
        combine(clientServiceFlow, geoHealthchecksFlow) { areServicesOk, areGeoHealthchecksOk ->
            areServicesOk to areGeoHealthchecksOk
        }.collectLatest { (areServicesOk, areGeoHealthchecksOk) ->
            logger.debug { "Client services are${serviceStatusToLog(areServicesOk)}up, geoHealthchecks are${serviceStatusToLog(areGeoHealthchecksOk)}up" }
            appStatus.isOk.set(areServicesOk || !areGeoHealthchecksOk)
        }
    }
}

private fun serviceStatusToLog(status: Boolean): String =
    " not ".takeIf { !status } ?: " "
