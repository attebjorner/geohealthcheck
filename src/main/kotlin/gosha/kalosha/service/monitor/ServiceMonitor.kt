package gosha.kalosha.service.monitor

import gosha.kalosha.properties.Service
import gosha.kalosha.service.RequestService
import gosha.kalosha.service.schedule.Scheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.flow

const val SERVICES_TASK = "services_status"

private fun getTaskName(service: Service) = "${SERVICES_TASK}_${service.name}"

class ServiceMonitor(
    private val scheduler: Scheduler,
    private val requestService: RequestService
) {

    fun checkServices(services: Collection<Service>, areUp: Array<Boolean>.() -> Boolean): Flow<Boolean> =
        combineTransform(createFlowForEachService(services)) { servicesStatuses ->
            emit(servicesStatuses.areUp())
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
