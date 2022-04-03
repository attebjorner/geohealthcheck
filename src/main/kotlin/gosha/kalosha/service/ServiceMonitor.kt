package gosha.kalosha.service

import gosha.kalosha.config.YamlProperties
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object ServiceMonitor : KoinComponent {

    private val client by inject<HttpClient>()
    private val properties by inject<YamlProperties>()
    private val services = properties.clientServices.serviceList
    private val failureThreshold = properties.failureThreshold

    suspend fun askServices() {
        for (service in services) {
            try {
                val response: HttpResponse = client.get(service.path + service.port)
                if (response.status.isSuccess() && response.content.readUTF8Line()!! == "OK") {
                    println("ok $service")
                    continue
                } else {
                    println("error $service")
                }
            } catch (ex: ClientRequestException) {
                println("error $service")
            }
        }
    }
}