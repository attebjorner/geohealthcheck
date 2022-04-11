package gosha.kalosha.properties

import io.ktor.http.*
import org.hamcrest.CoreMatchers.startsWith
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import kotlin.test.assertFails

internal class PropertiesLoaderTest {

    @Test
    fun `should parse new properties when backwardCompatibility is false`() {
        val propertiesLoader = PropertiesLoader(false, "new_application.yaml")
        val properties = propertiesLoader.load()
        properties.clientServices.services.forEach {
            assertThat(Url(it.endpoint).host, startsWith("new"))
        }
        properties.geoHealthchecks.forEach {
            assertThat(Url(it.endpoint).host, startsWith("new"))
        }
    }

    @Test
    fun `should parse old properties when backwardCompatibility is true`() {
        val propertiesLoader = PropertiesLoader(true, "old_application.yaml")
        val properties = propertiesLoader.load()
        properties.clientServices.services.forEach {
            assertThat(Url(it.endpoint).host, startsWith("old"))
        }
        properties.geoHealthchecks.forEach {
            assertThat(Url(it.endpoint).host, startsWith("old"))
        }
    }

    @Test
    fun `should fail when cannot parse`() {
        val propertiesLoader = PropertiesLoader(true, "new_application.yaml")
        assertFails { propertiesLoader.load() }
    }
}