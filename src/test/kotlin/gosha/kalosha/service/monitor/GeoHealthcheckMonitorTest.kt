package gosha.kalosha.service.monitor

import gosha.kalosha.properties.*
import gosha.kalosha.service.RequestService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.inject
import org.koin.test.junit5.AutoCloseKoinTest

internal class GeoHealthcheckMonitorTest : AutoCloseKoinTest() {

    private val testHealthcheck1 = GeoHealthcheck("servicename1", 80)

    private val testHealthcheck2 = GeoHealthcheck("servicename2", 80)

    private val testHealthcheck3 = GeoHealthcheck("servicename3", 80)

    private val testProperties = AppProperties(
        Logging(Level(LoggingLevel.INFO)),
        Schedule(true, 1),
        ClientServices(setOf()),
        listOf()
    )

    private val requestService: RequestService = mockk()

    private val geoHealthcheckMonitor by inject<GeoHealthcheckMonitor>()

    private fun startKoin() {
        startKoin {
            modules(
                module {
                    single { testProperties }
                    single { requestService }
                    single { GeoHealthcheckMonitor(get(), get()) }
                }
            )
        }
    }

    @BeforeEach
    fun setUp() {
        coEvery { requestService.isStatusUp(testHealthcheck1) } returns true
        coEvery { requestService.isStatusUp(testHealthcheck2) } returns false
        coEvery { requestService.isStatusUp(testHealthcheck3) } returns false
        stopKoin()
    }

    @Test
    fun `should emit true when checker returns OK`() = runBlocking {
        testProperties.geoHealthchecks = listOf(testHealthcheck1)
        startKoin()
        assertThat(geoHealthcheckMonitor.areGeoHealthchecksUp(), equalTo(true))
        coVerify { requestService.isStatusUp(testHealthcheck1) }
    }

    @Test
    fun `should emit false when checker returns not OK`() = runBlocking {
        testProperties.geoHealthchecks = listOf(testHealthcheck2)
        startKoin()
        assertThat(geoHealthcheckMonitor.areGeoHealthchecksUp(), equalTo(false))
        coVerify { requestService.isStatusUp(testHealthcheck2) }
    }

    @Test
    fun `should emit true when at least one geohealthcheck is OK`() = runBlocking {
        testProperties.geoHealthchecks = listOf(testHealthcheck1, testHealthcheck2, testHealthcheck3)
        startKoin()
        assertThat(geoHealthcheckMonitor.areGeoHealthchecksUp(), equalTo(true))
        testProperties.geoHealthchecks.forEach {
            coVerify { requestService.isStatusUp(it) }
        }
    }

    @Test
    fun `should emit false when all geohealthchecks are not OK`() = runBlocking {
        testProperties.geoHealthchecks = listOf(testHealthcheck3, testHealthcheck2)
        startKoin()
        assertThat(geoHealthcheckMonitor.areGeoHealthchecksUp(), equalTo(false))
        testProperties.geoHealthchecks.forEach {
            coVerify { requestService.isStatusUp(it) }
        }
    }
}