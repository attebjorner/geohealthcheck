package gosha.kalosha.service.monitor

import gosha.kalosha.properties.AppProperties
import gosha.kalosha.properties.ClientService
import gosha.kalosha.service.RequestService
import gosha.kalosha.service.schedule.Scheduler
import kotlinx.coroutines.flow.*

const val CLIENT_SERVICES_TASK = "client_services_status"

class ClientServiceMonitor(
    properties: AppProperties,
    private val scheduler: Scheduler,
    private val requestService: RequestService
) {

    private val services = properties.clientServices.services

    fun checkServices(): Flow<Boolean> =
        combineTransform(createFlowForEachService()) { servicesStatuses ->
            val areServicesUp = servicesStatuses.all { isUp -> isUp }
            emit(areServicesUp)
            if (!areServicesUp) {
                stopChecking()
            }
        }

    private fun createFlowForEachService(): Collection<Flow<Boolean>> =
        services.map { service ->
            flow {
                scheduler.createTask(getTaskName(service), service.delay) {
                    emit(isStatusUp(service))
                }.start()
            }
        }

    private suspend fun isStatusUp(service: ClientService): Boolean {
        if (!requestService.isStatusUp(service)) {
            ++service.timesFailed
        }
        return service.timesFailed != service.failureThreshold
    }

    private fun stopChecking() {
        for (service in services) {
            scheduler.findTask(getTaskName(service)).shutdown()
        }
    }

    private fun getTaskName(service: ClientService) = "${CLIENT_SERVICES_TASK}_${service.endpoint}"
}