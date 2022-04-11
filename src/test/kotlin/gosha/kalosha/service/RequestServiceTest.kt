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
    fun `should increase timesFailed when returned not 200`() = runBlocking {
        val timesFailed = testHealthcheck2.timesFailed
        requestService.updateStatus(testHealthcheck2)
        assertThat(testHealthcheck2.timesFailed, equalTo(timesFailed + 1))
    }

    @Test
    fun `should reset timesFailed when returned true after fail`() = runBlocking {
        testHealthcheck1.timesFailed = testHealthcheck1.failureThreshold + 1
        requestService.updateStatus(testHealthcheck1)
        assertThat(testHealthcheck1.timesFailed, equalTo(0))
    }
}