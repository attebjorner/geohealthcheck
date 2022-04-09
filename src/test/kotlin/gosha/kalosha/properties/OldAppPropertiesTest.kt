package gosha.kalosha.properties

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

internal class OldAppPropertiesTest {

    @Test
    fun `should convert old properties to new with duplicated services`() {
        val service = Service("s1", "80", "/")
        val oldProperties = OldAppProperties(
            Logging(Level(LoggingLevel.DEBUG)),
            Schedule(true, 100),
            ClientServices(listOf(service)),
            failureThreshold = 4,
            GeoHealthcheck("geo", "80")
        )
        val properties = oldProperties.toProperties()

        assertThat(properties.logging, equalTo(oldProperties.logging))
        assertThat(properties.schedule, equalTo(oldProperties.schedule))
        assertThat(properties.clientServices.services.size, equalTo(1))
        properties.clientServices.services.forEach {
            assertThat(it.serviceName, equalTo(service.serviceName))
            assertThat(it.port, equalTo(service.port))
            assertThat(it.path, equalTo(service.path))
            assertThat(it.delay, equalTo(oldProperties.schedule.delay))
            assertThat(it.failureThreshold, equalTo(oldProperties.failureThreshold))
        }
        assertThat(properties.geoHealthchecks.size, equalTo(1))
        properties.geoHealthchecks.forEach {
            assertThat(it, equalTo(oldProperties.geoHealthcheck))
        }
    }
}