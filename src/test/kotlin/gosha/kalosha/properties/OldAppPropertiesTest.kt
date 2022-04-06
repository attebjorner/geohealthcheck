package gosha.kalosha.properties

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import kotlin.test.Test

internal class OldAppPropertiesTest {

    @Test
    fun `should convert old properties to new with duplicated services`() {
        val service = Service("s1", "80", "/")
        val oldProperties = OldAppProperties(
            Logging(Level(LoggingLevel.DEBUG)),
            Schedule(true, 100),
            OldClientServices(listOf(service.copy(), service.copy())),
            failureThreshold = 4,
            GeoHealthcheck("geo", "80")
        )
        val properties = oldProperties.toProperties()

        assertThat(properties.logging, equalTo(oldProperties.logging))
        assertThat(properties.schedule, equalTo(oldProperties.schedule))
        assertThat(properties.clientServices.serviceSet.size, equalTo(1))
        properties.clientServices.serviceSet.forEach {
            assertThat(it.serviceName, equalTo(service.serviceName))
            assertThat(it.port, equalTo(service.port))
            assertThat(it.path, equalTo(service.path))
            assertThat(it.delay, equalTo(oldProperties.schedule.delay))
            assertThat(it.failureThreshold, equalTo(oldProperties.failureThreshold))
        }
        assertThat(properties.geoHealthcheckList.size, equalTo(1))
        assertThat(properties.geoHealthcheckList[0], equalTo(oldProperties.geoHealthcheck))
    }
}