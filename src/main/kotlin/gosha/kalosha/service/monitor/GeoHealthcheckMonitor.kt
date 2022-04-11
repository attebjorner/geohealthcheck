package gosha.kalosha.service.monitor

import gosha.kalosha.properties.AppProperties
import gosha.kalosha.service.RequestService
import kotlinx.coroutines.delay

class GeoHealthcheckMonitor(
    properties: AppProperties,
    private val requestService: RequestService
) {

    private val delay = properties.schedule.delay

    private val geoHealthchecks = properties.geoHealthchecks

    suspend fun areGeoHealthchecksUp(): Boolean {
        foreach@ for (geoHealthcheck in geoHealthchecks) {
            while (geoHealthcheck.timesFailed != geoHealthcheck.failureThreshold) {
                if (requestService.isStatusUp(geoHealthcheck)) {
                    continue@foreach
                }
                ++geoHealthcheck.timesFailed
                delay(delay)
            }
        }
        return geoHealthchecks.any { it.timesFailed != it.failureThreshold }
    }
}
