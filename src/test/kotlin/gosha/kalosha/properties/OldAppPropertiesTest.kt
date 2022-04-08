package gosha.kalosha.properties

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import kotlin.test.Test

internal class OldAppPropertiesTest {

    @Test
    fun `should convert old properties to new with duplicated services`() {
        val clientService = ClientService("s1", "80", "/")
        val oldProperties = OldAppProperties(
            Logging(Level(LoggingLevel.DEBUG)),
            Schedule(true, 100),
            ClientServices(listOf(clientService)),
            failureThreshold = 4,
            GeoHealthcheck("geo", "80")
        )
        val properties = oldProperties.toProperties()

        assertThat(properties.logging, equalTo(oldProperties.logging))
        assertThat(properties.schedule, equalTo(oldProperties.schedule))
        assertThat(properties.clientServices.clientServices.size, equalTo(1))
        properties.clientServices.clientServices.forEach {
            assertThat(it.serviceName, equalTo(clientService.serviceName))
            assertThat(it.port, equalTo(clientService.port))
            assertThat(it.path, equalTo(clientService.path))
            assertThat(it.delay, equalTo(oldProperties.schedule.delay))
            assertThat(it.failureThreshold, equalTo(oldProperties.failureThreshold))
        }
        assertThat(properties.geoHealthchecks.size, equalTo(1))
        properties.geoHealthchecks.forEach {
            assertThat(it, equalTo(oldProperties.geoHealthcheck))
        }
    }
}