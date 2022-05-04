package gosha.kalosha.service.monitor

import dev.inmo.krontab.builder.buildSchedule
import dev.inmo.krontab.doInfinity
import gosha.kalosha.properties.Service
import gosha.kalosha.service.RequestService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.flow

class ServiceMonitor(
    private val requestService: RequestService
) {

    fun checkServices(services: Collection<Service>, areUp: Array<Boolean>.() -> Boolean): Flow<Boolean> =
        combineTransform(createFlowForEachService(services)) { servicesStatuses ->
            emit(servicesStatuses.areUp())
        }

    private fun createFlowForEachService(services: Collection<Service>): Collection<Flow<Boolean>> =
        services.map { service ->
            flow {
                scheduleInfinity(service.delay) {
                    requestService.updateStatus(service)
                    emit(service.isUp)
                }
            }
        }
}

private suspend fun scheduleInfinity(delay: Long, action: suspend () -> Unit) {
    buildSchedule {
        milliseconds {
            from (0) every delay.toInt()
        }
    }.doInfinity {
        action()
    }
}