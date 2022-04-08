package gosha.kalosha.properties

import org.hamcrest.CoreMatchers.startsWith
import org.hamcrest.MatcherAssert.assertThat
import kotlin.test.Test
import kotlin.test.assertFails

internal class PropertiesLoaderTest {

    @Test
    fun `should parse new properties when backwardCompatibility is false`() {
        val propertiesLoader = PropertiesLoader(false, "new_application.yaml")
        val properties = propertiesLoader.load()
        properties.geoHealthchecks.forEach {
            assertThat(it.serviceName, startsWith("new"))
        }
    }

    @Test
    fun `should parse old properties when backwardCompatibility is true`() {
        val propertiesLoader = PropertiesLoader(true, "old_application.yaml")
        val properties = propertiesLoader.load()
        properties.geoHealthchecks.forEach {
            assertThat(it.serviceName, startsWith("old"))
        }
    }

    @Test
    fun `should fail when cannot parse`() {
        val propertiesLoader = PropertiesLoader(true, "new_application.yaml")
        assertFails { propertiesLoader.load() }
    }
}