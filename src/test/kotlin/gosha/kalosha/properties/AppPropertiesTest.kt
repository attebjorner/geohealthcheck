package gosha.kalosha.properties

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.slf4j.event.Level

internal class AppPropertiesTest {

    @Test
    fun `should covert LoggingLevel to slf4j Level`() {
        val loggingLevel = LoggingLevel.WARN
        val slf4jLevel = loggingLevel.toSlf4jLevel()
        assertThat(slf4jLevel, equalTo(Level.WARN))
    }

    @Test
    fun `should throw exception when services contains duplicates`() {
        assertThrows<RuntimeException> { AppProperties(
            Logging(Level(LoggingLevel.DEBUG)),
            Schedule(true, 100),
            ClientServices(listOf(ClientService("s1", 80, "/"), ClientService("s1", 80, "/"))),
            listOf(GeoHealthcheck("geo", 80))
        ) }
    }

    @Test
    fun `should throw exception when geoHealthchecks contains duplicates`() {
        assertThrows<RuntimeException> { AppProperties(
            Logging(Level(LoggingLevel.DEBUG)),
            Schedule(true, 100),
            ClientServices(listOf(ClientService("s1", 80, "/"))),
            listOf(GeoHealthcheck("geo", 80), GeoHealthcheck("geo", 80))
        ) }
    }
}