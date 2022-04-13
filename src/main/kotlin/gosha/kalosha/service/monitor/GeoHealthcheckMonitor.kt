package gosha.kalosha.service.monitor

import gosha.kalosha.properties.AppProperties
import gosha.kalosha.service.RequestService
import gosha.kalosha.service.schedule.Scheduler
import kotlinx.coroutines.flow.flow

const val GEOHEALTHCHECKS_TASK = "geohealthchecks_status"

class GeoHealthcheckMonitor(
    properties: AppProperties,
    private val scheduler: Scheduler,
    private val requestService: RequestService
) {

    private val delay = properties.schedule.delay

    private val geoHealthchecks = properties.geoHealthchecks

    fun checkGeoHealthcheckStatus() = flow {
        scheduler.createTask(GEOHEALTHCHECKS_TASK, delay) {
            updateGeoHealthchecks()
            emit(geoHealthchecks.any { it.isUp })
        }.start()
    }

    private suspend fun updateGeoHealthchecks() {
        for (geoHealthcheck in geoHealthchecks) {
            requestService.updateStatus(geoHealthcheck)
        }
    }
}
