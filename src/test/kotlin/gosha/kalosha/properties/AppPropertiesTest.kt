package gosha.kalosha.properties

import gosha.kalosha.exception.DublicateServiceException
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
        assertThrows<DublicateServiceException> { AppProperties(
            Logging(Level(LoggingLevel.DEBUG)),
            Schedule(true),
            ClientServices(listOf(Service("s1", 80), Service("s1", 80))),
            listOf(Service("geo", 80))
        ) }
    }

    @Test
    fun `should throw exception when geoHealthchecks contains duplicates`() {
        assertThrows<DublicateServiceException> { AppProperties(
            Logging(Level(LoggingLevel.DEBUG)),
            Schedule(true),
            ClientServices(listOf(Service("s1", 80))),
            listOf(Service("geo", 80), Service("geo", 80))
        ) }
    }
}