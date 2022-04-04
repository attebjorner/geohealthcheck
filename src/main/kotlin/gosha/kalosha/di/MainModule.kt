package gosha.kalosha.di

import com.charleskorn.kaml.Yaml
import gosha.kalosha.config.AppStatus
import gosha.kalosha.config.AppProperties
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.serialization.decodeFromString
import org.koin.dsl.module

fun mainModule(namespace: String) = module {
    single { yamlProperties(this@module) }
    single { HttpClient(CIO) }
    single { AppStatus(namespace) }
}

fun yamlProperties(classProv: Any): AppProperties {
    val yamlParser = Yaml(configuration = Yaml.default.configuration.copy(strictMode = false))
    val propertiesFile = classProv::class.java.getResource("/application.yaml")!!
    return yamlParser.decodeFromString(propertiesFile.readText())
}
