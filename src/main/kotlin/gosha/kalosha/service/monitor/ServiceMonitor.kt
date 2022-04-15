package gosha.kalosha.service.monitor

import gosha.kalosha.properties.AppProperties
import gosha.kalosha.properties.Service
import gosha.kalosha.service.RequestService
import gosha.kalosha.service.schedule.Scheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.flow

const val SERVICES_TASK = "services_status"

private fun getTaskName(service: Service) = "${SERVICES_TASK}_${service.name}"

class ServiceMonitor(
    properties: AppProperties,
    private val scheduler: Scheduler,
    private val requestService: RequestService
) {

    private val services = properties.clientServices.services

    private val geoHealthchecks = properties.geoHealthchecks

    fun checkClientServices(): Flow<Boolean> =
        checkServices(services) { statuses ->
            statuses.all { isUp -> isUp }
        }

    fun checkGeoHealthchecks(): Flow<Boolean> =
        checkServices(geoHealthchecks) { statuses ->
            statuses.any { isUp -> isUp }
        }

    private fun checkServices(
        services: Collection<Service>,
        predicate: (Array<Boolean>) -> Boolean
    ): Flow<Boolean> =
        combineTransform(createFlowForEachService(services)) { servicesStatuses ->
            emit(predicate(servicesStatuses))
        }

    private fun createFlowForEachService(services: Collection<Service>): Collection<Flow<Boolean>> =
        services.map { service ->
            flow {
                scheduler.createTask(getTaskName(service), service.delay) {
                    requestService.updateStatus(service)
                    emit(service.isUp)
                }.start()
            }
        }
}
