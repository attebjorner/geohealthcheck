package gosha.kalosha.service

import gosha.kalosha.properties.AppProperties
import gosha.kalosha.service.schedule.Scheduler
import kotlinx.coroutines.flow.flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

const val CLIENT_SERVICES_TASK = "services status"

class ClientServiceMonitor : KoinComponent {

    private val properties by inject<AppProperties>()

    private val scheduler by inject<Scheduler>()

    private val services = properties.clientServices.clientServices

    private val checker = ServicesChecker(
        services = services,
        url = { service -> "http://${service.serviceName}:${service.port}${service.path}" },
        onError = { service -> --service.failureThreshold },
        check = { services.all { it.failureThreshold != 0 } }
    )

    fun checkServices() = flow {
        scheduler.createTask(CLIENT_SERVICES_TASK, properties.schedule.delay) {
            val isUp = checker.isStatusUp()
            emit(isUp)
            if (!isUp) {
                scheduler.findTask(CLIENT_SERVICES_TASK).shutdown()
            }
        }.schedule()
    }
}