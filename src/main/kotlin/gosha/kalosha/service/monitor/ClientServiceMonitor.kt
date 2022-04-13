package gosha.kalosha.service.monitor

import gosha.kalosha.properties.AppProperties
import gosha.kalosha.properties.ClientService
import gosha.kalosha.service.RequestService
import gosha.kalosha.service.schedule.Scheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.flow

const val CLIENT_SERVICES_TASK = "client_services_status"

private fun getTaskName(service: ClientService) = "${CLIENT_SERVICES_TASK}_${service.name}"

class ClientServiceMonitor(
    properties: AppProperties,
    private val scheduler: Scheduler,
    private val requestService: RequestService
) {

    private val services = properties.clientServices.services

    fun checkServices(): Flow<Boolean> =
        combineTransform(createFlowForEachService()) { servicesStatuses ->
            emit(servicesStatuses.all { isUp -> isUp })
        }

    private fun createFlowForEachService(): Collection<Flow<Boolean>> =
        services.map { service ->
            flow {
                scheduler.createTask(getTaskName(service), service.delay) {
                    requestService.updateStatus(service)
                    emit(service.isUp)
                }.start()
            }
        }
}
