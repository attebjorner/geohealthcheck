package gosha.kalosha.service.monitor

import gosha.kalosha.entity.AppStatus
import gosha.kalosha.properties.AppProperties
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import mu.KotlinLogging

class AppStatusMonitor(
    private val appStatus: AppStatus,
    private val serviceMonitor: ServiceMonitor,
    properties: AppProperties
) {
    private val logger = KotlinLogging.logger {  }

    private val services = properties.clientServices.services

    private val geoHealthchecks = properties.geoHealthchecks

    suspend fun startMonitoring() {
        val clientServiceFlow = serviceMonitor.checkServices(services) { all { isUp -> isUp } }
        val geoHealthchecksFlow = serviceMonitor.checkServices(geoHealthchecks) { any { isUp -> isUp } }
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
