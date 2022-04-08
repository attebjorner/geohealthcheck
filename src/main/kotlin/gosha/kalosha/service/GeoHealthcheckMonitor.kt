package gosha.kalosha.service

import gosha.kalosha.properties.AppProperties
import gosha.kalosha.service.schedule.Scheduler
import kotlinx.coroutines.flow.flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

const val GEOHEALTHCHECKS_TASK = "geohealthchecks_status"

class GeoHealthcheckMonitor : KoinComponent {

    private val properties by inject<AppProperties>()

    private val scheduler by inject<Scheduler>()

    private val geoHealthchecks = properties.geoHealthchecks

    private val checker = ServicesChecker(
        services = geoHealthchecks,
        url = { geoHealthcheck -> "http://${geoHealthcheck.serviceName}:${geoHealthcheck.port}/health" },
        onSuccess = { geoHealthcheck -> geoHealthcheck.isOk = true },
        onError = { geoHealthcheck -> geoHealthcheck.isOk = false },
        check = { geoHealthchecks.any { it.isOk } }
    )

    fun checkGeoHealthcheckStatus() = flow {
        scheduler.createTask(GEOHEALTHCHECKS_TASK, properties.schedule.delay) {
            emit(checker.isStatusUp())
        }.schedule()
    }
}
