package gosha.kalosha.service

import gosha.kalosha.properties.AppProperties
import gosha.kalosha.properties.ClientService
import gosha.kalosha.service.schedule.Scheduler
import kotlinx.coroutines.flow.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

const val CLIENT_SERVICES_TASK = "client_services_status"

class ClientServiceMonitor : KoinComponent {

    private val properties by inject<AppProperties>()

    private val scheduler by inject<Scheduler>()

    private val services = properties.clientServices.clientServices

    private val checkers = services.associateWith { service ->
        SingleServiceChecker(
            service = service,
            url = "http://${service.serviceName}:${service.port}${service.path}",
            onError = { ++service.timesFailed },
            check = { service.timesFailed != service.failureThreshold }
        )
    }

    fun checkServices() = channelFlow {
        val serviceStatusFlows = createFlowForEachService()
        combine(serviceStatusFlows) { servicesStatuses ->
            servicesStatuses.all { isUp -> isUp }
        }.collectLatest { areServicesUp ->
            send(areServicesUp)
            if (!areServicesUp) {
                removeTasks()
            }
        }
    }

    private fun getServiceTaskName(service: ClientService) = "${CLIENT_SERVICES_TASK}_${service.serviceName}"

    private fun createFlowForEachService(): Collection<Flow<Boolean>> {
        var checkerFlows = setOf<Flow<Boolean>>()
        for (serviceChecker in checkers) {
            val service = serviceChecker.key
            val checker = serviceChecker.value
            val taskName = getServiceTaskName(service)
            checkerFlows = checkerFlows + flow {
                scheduler.createTask(taskName, service.delay) {
                    emit(checker.isStatusUp())
                }.schedule()
            }
        }
        return checkerFlows
    }

    private fun removeTasks() {
        for (service in services) {
            scheduler.findTask(getServiceTaskName(service)).shutdown()
        }
    }
}