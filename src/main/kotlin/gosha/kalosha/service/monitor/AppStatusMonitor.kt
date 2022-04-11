package gosha.kalosha.service.monitor

import gosha.kalosha.properties.AppStatus
import kotlinx.coroutines.flow.collectLatest

class AppStatusMonitor(
    private val appStatus: AppStatus,
    private val clientServiceMonitor: ClientServiceMonitor,
    private val geoHealthcheckMonitor: GeoHealthcheckMonitor
) {

    suspend fun startMonitoring() {
        val clientServiceFlow = clientServiceMonitor.checkServices()
        clientServiceFlow.collectLatest { areServicesOk ->
            if (!areServicesOk && geoHealthcheckMonitor.areGeoHealthchecksUp()) {
                appStatus.isOk.set(false)
            }
        }
    }
}
