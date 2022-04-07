package gosha.kalosha.service

import gosha.kalosha.properties.AppProperties
import gosha.kalosha.properties.AppStatus
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GeoHealthcheckMonitor : KoinComponent {

    private val properties by inject<AppProperties>()

    private val appStatus by inject<AppStatus>()

    private val geoHealthchecks = properties.geoHealthcheckList

    private val checker = ServicesChecker(
        services = geoHealthchecks,
        url = { geoHealthcheck -> "http://${geoHealthcheck.serviceName}:${geoHealthcheck.port}/health" },
        onSuccess = { geoHealthcheck -> geoHealthcheck.isOk = true },
        onError = { geoHealthcheck -> geoHealthcheck.isOk = false },
        check = { geoHealthchecks.any { it.isOk } }
    )

    suspend fun checkGeoHealthcheckStatus() {
        val areGeoHealthchecksUp = checker.isStatusUp()
        appStatus.geoHealthcheckIsOk.set(areGeoHealthchecksUp)
    }
}
