package gosha.kalosha.properties

import com.charleskorn.kaml.Yaml
import gosha.kalosha.exception.LoadPropertiesException
import mu.KotlinLogging
import java.io.File

class PropertiesLoader(
    private val backwardCompatibility: Boolean,
    propertiesFilePath: String?
) {
    private val logger = KotlinLogging.logger {  }

    private val yamlParser = Yaml(configuration = Yaml.default.configuration.copy(strictMode = false))

    private val propertiesFile = File(propertiesFilePath ?: this::class.java.classLoader.getResource("application.yaml")!!.path)

    fun load(): AppProperties {
        return try {
            if (backwardCompatibility) {
                // явный serializer() для грааля
                yamlParser.decodeFromString(OldAppProperties.serializer(), propertiesFile.readText()).toProperties()
            } else {
                yamlParser.decodeFromString(AppProperties.serializer(), propertiesFile.readText())
            }
        } catch (ex: Exception) {
            throw LoadPropertiesException("Could not parse properties", ex)
        }
    }
}
