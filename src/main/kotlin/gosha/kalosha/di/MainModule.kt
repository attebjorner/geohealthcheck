package gosha.kalosha.di

import gosha.kalosha.properties.AppStatus
import gosha.kalosha.properties.PropertiesLoader
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import org.koin.dsl.module

fun mainModule(namespace: String, backwardCompatibility: Boolean) = module {
    single { PropertiesLoader(backwardCompatibility).load() }
    single { HttpClient(CIO) }
    single { AppStatus(namespace) }
}
