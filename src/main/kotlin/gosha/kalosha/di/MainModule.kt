package gosha.kalosha.di

import com.charleskorn.kaml.Yaml
import gosha.kalosha.model.YamlProperties
import kotlinx.serialization.decodeFromString
import org.koin.dsl.module

val mainModule = module {
    single { yamlProperties(this@module) }
}

fun yamlProperties(classProv: Any): YamlProperties {
    val propertiesFile = classProv::class.java.getResource("/application.yaml")!!
    return Yaml.default.decodeFromString(propertiesFile.readText())
}
