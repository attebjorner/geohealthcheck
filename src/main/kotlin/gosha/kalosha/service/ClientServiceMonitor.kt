package gosha.kalosha.service

import gosha.kalosha.properties.AppProperties
import gosha.kalosha.properties.Service
import gosha.kalosha.service.schedule.Scheduler
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.flow.*
import mu.KotlinLogging

const val CLIENT_SERVICES_TASK = "client_services_status"

class ClientServiceMonitor(
    properties: AppProperties,
    private val scheduler: Scheduler,
    private val client: HttpClient
) {

    private val logger = KotlinLogging.logger {  }

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

    private suspend fun isStatusUp(service: Service): Boolean {
        try {
            logger.info { "Sending healthcheck to '${service.serviceName}'" }
            val response: HttpResponse = client.get(service.url)
            logger.info { "Got ${response.status.value} status code from '${service.serviceName}'" }
        } catch (ex: ResponseException) {
            logger.info { "Got ${ex.response.status.value} status code from '${service.serviceName}'" }
            ++service.timesFailed
        }
        return service.timesFailed != service.failureThreshold
    }

    private fun stopChecking() {
        for (service in services) {
            scheduler.findTask(getTaskName(service)).shutdown()
        }
    }

    private fun getTaskName(service: Service) = "${CLIENT_SERVICES_TASK}_${service.serviceName}"
}