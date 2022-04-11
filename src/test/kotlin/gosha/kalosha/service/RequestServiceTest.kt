package gosha.kalosha.service

import gosha.kalosha.properties.GeoHealthcheck
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.test.junit5.AutoCloseKoinTest
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.inject

internal class RequestServiceTest : AutoCloseKoinTest() {

    private val testHealthcheck1 = GeoHealthcheck(URLBuilder(host = "service1").buildString())

    private val testHealthcheck2 = GeoHealthcheck(URLBuilder(host = "service2").buildString())

    private val client = HttpClient(MockEngine) {
        engine {
            addHandler { request ->
                when (request.url.toString()) {
                    testHealthcheck1.endpoint -> respond("ok", HttpStatusCode.OK)
                    testHealthcheck2.endpoint -> respond("not ok", HttpStatusCode.InternalServerError)
                    else -> error("Unhandled ${request.url}")
                }
            }
        }
    }

    private val requestService by inject<RequestService>()

    private fun startKoin() {
        startKoin {
            modules(
                module {
                    single { client }
                    single { RequestService(get()) }
                }
            )
        }
    }

    @BeforeEach
    fun setUp() {
        stopKoin()
        startKoin()
    }

    @Test
    fun `should return true when service returned 200`() = runBlocking {
        assertThat(requestService.isStatusUp(testHealthcheck1), equalTo(true))
    }

    @Test
    fun `should return false when service returned not 200`() = runBlocking {
        assertThat(requestService.isStatusUp(testHealthcheck2), equalTo(false))
    }
}