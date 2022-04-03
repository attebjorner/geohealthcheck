package gosha.kalosha.di

import com.charleskorn.kaml.Yaml
import gosha.kalosha.config.YamlProperties
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.serialization.decodeFromString
import org.koin.dsl.module
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

fun mainModule(coroutineContext: CoroutineContext) = module {
    single { yamlProperties(this@module) }
    single { appScope(coroutineContext) }
    single { HttpClient(CIO) }
}

fun yamlProperties(classProv: Any): YamlProperties
{
    val propertiesFile = classProv::class.java.getResource("/application.yaml")!!
    return Yaml.default.decodeFromString(propertiesFile.readText())
}

fun appScope(coroutineContext: CoroutineContext) =
    CoroutineScope(coroutineContext)
