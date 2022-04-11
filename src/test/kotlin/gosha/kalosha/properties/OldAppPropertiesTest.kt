package gosha.kalosha.properties

import io.ktor.http.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

internal class OldAppPropertiesTest {

    @Test
    fun `should convert old properties to new with duplicated services`() {
        val serviceUrl = URLBuilder(URLProtocol.HTTP, host = "service", port = 80, encodedPath = "abc")
        val geoHealthcheckUrl = URLBuilder(URLProtocol.HTTP, host = "geoHealthcheck", port = 81, encodedPath = "health")
        val oldProperties = OldAppProperties(
            Logging(Level(LoggingLevel.DEBUG)),
            Schedule(true, 100),
            OldClientServices(
                listOf(
                    OldService(
                        serviceName = serviceUrl.host,
                        port = serviceUrl.port,
                        path = serviceUrl.encodedPath
                    )
                )
            ),
            failureThreshold = 4,
            OldGeoHealthcheck(serviceName = geoHealthcheckUrl.host, port = geoHealthcheckUrl.port)
        )
        val properties = oldProperties.toProperties()

        assertThat(properties.logging, equalTo(oldProperties.logging))
        assertThat(properties.schedule, equalTo(oldProperties.schedule))
        assertThat(properties.clientServices.services.size, equalTo(1))
        properties.clientServices.services.forEach {
            assertThat(it.endpoint, equalTo(serviceUrl.buildString()))
            assertThat(it.delay, equalTo(oldProperties.schedule.delay))
            assertThat(it.failureThreshold, equalTo(oldProperties.failureThreshold))
        }
        assertThat(properties.geoHealthchecks.size, equalTo(1))
        properties.geoHealthchecks.forEach {
            assertThat(it.endpoint, equalTo(geoHealthcheckUrl.buildString()))
            assertThat(it.failureThreshold, equalTo(oldProperties.failureThreshold))
        }
    }
}