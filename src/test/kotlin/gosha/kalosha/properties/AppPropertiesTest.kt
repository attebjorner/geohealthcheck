package gosha.kalosha.properties

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.slf4j.event.Level
import kotlin.test.Test

internal class AppPropertiesTest {

    @Test
    fun `should covert LoggingLevel to slf4j Level`() {
        val loggingLevel = LoggingLevel.WARN
        val slf4jLevel = loggingLevel.toSlf4jLevel()
        assertThat(slf4jLevel, equalTo(Level.WARN))
    }
}