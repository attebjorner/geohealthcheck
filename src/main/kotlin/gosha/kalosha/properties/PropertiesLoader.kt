package gosha.kalosha.properties

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString

class PropertiesLoader(
    private val backwardCompatibility: Boolean,
    propertiesFile: String = "application.yaml"
) {
    private val yamlParser = Yaml(configuration = Yaml.default.configuration.copy(strictMode = false))

    private val propertiesUrl = this::class.java.getResource("/${propertiesFile}")!!

    fun load(): AppProperties {
        return if (backwardCompatibility) {
            yamlParser.decodeFromString<OldAppProperties>(propertiesUrl.readText()).toProperties()
        } else {
            yamlParser.decodeFromString<AppProperties>(propertiesUrl.readText())
        }
    }
}
