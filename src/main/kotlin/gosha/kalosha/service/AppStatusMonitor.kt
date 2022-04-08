package gosha.kalosha.service

import gosha.kalosha.properties.AppProperties
import gosha.kalosha.properties.AppStatus
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AppStatusMonitor : KoinComponent {

    private val logger = KotlinLogging.logger {  }

    private val appStatus by inject<AppStatus>()

    private val clientServiceMonitor by inject<ClientServiceMonitor>()

    private val geoHealthcheckMonitor by inject<GeoHealthcheckMonitor>()

    suspend fun startMonitoring() {
        val clientServiceFlow = clientServiceMonitor.checkServices()
        val geoHealthcheckFlow = geoHealthcheckMonitor.checkGeoHealthcheckStatus()
        combine(clientServiceFlow, geoHealthcheckFlow) { areServicesOk, areGeoHealthchecksOk ->
            areServicesOk to areGeoHealthchecksOk
        }.collectLatest { (areServicesOk, areGeoHealthchecksOk) ->
            logger.info { "collecting $areServicesOk $areGeoHealthchecksOk" } //todo remove this
            appStatus.isOk.set(areServicesOk || !areGeoHealthchecksOk)
        }
    }
}