package gosha.kalosha.properties

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import mu.KotlinLogging
import java.io.File

class PropertiesLoader(
    private val backwardCompatibility: Boolean,
    propertiesFilePath: String?
) {
    private val logger = KotlinLogging.logger {  }

    private val yamlParser = Yaml(configuration = Yaml.default.configuration.copy(strictMode = false))

    init {
        logger.info { propertiesFilePath }
        logger.info { this::class.java.classLoader.getResource("application.yaml") }
    }

    private val propertiesFile = File(propertiesFilePath ?: this::class.java.classLoader.getResource("application.yaml")!!.path)

    fun load(): AppProperties {
        return try {
            if (backwardCompatibility) {
                yamlParser.decodeFromString<OldAppProperties>(propertiesFile.readText()).toProperties()
            } else {
                yamlParser.decodeFromString<AppProperties>(propertiesFile.readText())
            }
        } catch (ex: Exception) {
            logger.error { "Could not parse properties" }
            throw ex
        }
    }
}
