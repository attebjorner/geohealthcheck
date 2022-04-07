package gosha.kalosha.service

import gosha.kalosha.properties.AppProperties
import gosha.kalosha.properties.AppStatus
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ClientServiceMonitor : KoinComponent {

    private val properties by inject<AppProperties>()

    private val appStatus by inject<AppStatus>()

    private val services = properties.clientServices.clientServiceSet

    private val checker = ServicesChecker(
        services = services,
        url = { service -> "http://${service.serviceName}:${service.port}${service.path}" },
        onError = { service -> --service.failureThreshold },
        check = { services.all { it.failureThreshold != 0 } }
    )

    suspend fun checkServices() {
        val areServicesUp = checker.isStatusUp()
        appStatus.isOk.set(!appStatus.geoHealthcheckIsOk.get() || areServicesUp)
    }
}