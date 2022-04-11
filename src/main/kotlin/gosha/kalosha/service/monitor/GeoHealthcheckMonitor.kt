package gosha.kalosha.service.monitor

import gosha.kalosha.properties.AppProperties
import gosha.kalosha.service.RequestService
import gosha.kalosha.service.schedule.Scheduler
import kotlinx.coroutines.delay
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
            emit(areGeoHealthchecksUp())
        }.start()
    }

    private suspend fun areGeoHealthchecksUp(): Boolean {
        for (geoHealthcheck in geoHealthchecks) {
            requestService.updateStatus(geoHealthcheck)
        }
        return geoHealthchecks.any { it.timesFailed < it.failureThreshold }
    }
}
