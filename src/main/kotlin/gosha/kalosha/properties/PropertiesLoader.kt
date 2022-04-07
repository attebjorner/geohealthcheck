package gosha.kalosha.properties

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import mu.KotlinLogging

class PropertiesLoader(
    private val backwardCompatibility: Boolean,
    propertiesFile: String = "application.yaml"
) {
    private val logger = KotlinLogging.logger {  }

    private val yamlParser = Yaml(configuration = Yaml.default.configuration.copy(strictMode = false))

    private val propertiesUrl = this::class.java.getResource("/${propertiesFile}")!!

    fun load(): AppProperties {
        return try {
            if (backwardCompatibility) {
                yamlParser.decodeFromString<OldAppProperties>(propertiesUrl.readText()).toProperties()
            } else {
                yamlParser.decodeFromString<AppProperties>(propertiesUrl.readText())
            }
        } catch (ex: Exception) {
            logger.error { "Could not parse properties" }
            throw ex
        }
    }
}
