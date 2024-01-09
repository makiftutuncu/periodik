package dev.akif.periodik

import kotlinx.coroutines.delay
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

class PeriodikTest {
    private class WeatherService(
        private val clock: Clock,
        private val loadCities: suspend () -> Map<String, Long>,
        private val loadWeather: () -> Map<Long, Double>,
    ) {
        private val cities by Periodik.suspending(
            schedule = Schedule.every(1.days),
            adjustment = {
                LocalDateTime
                    .ofInstant(it, ZoneOffset.UTC)
                    .withHour(0)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0)
                    .toInstant(ZoneOffset.UTC)
            },
            currentInstant = { clock.instant() }
        ) {
            loadCities()
        }

        private val weather by Periodik.default(
            schedule = Schedule.every(2.hours),
            deferInitialization = true,
            currentInstant = { clock.instant() }
        ) {
            loadWeather()
        }

        fun weatherFor(city: String): Double? =
            cities[city]?.let { id -> weather[id] }
    }

    private suspend fun dummyLoadCities(iteration: Int): Map<String, Long> {
        delay(100L * iteration)
        return when (iteration) {
            0 -> emptyMap()
            1 -> mapOf("Amsterdam" to 1L)
            2 -> mapOf("Amsterdam" to 1L, "Istanbul" to 2L)
            else -> mapOf("Amsterdam" to 1L, "Istanbul" to 2L, "Berlin" to 3L)
        }
    }

    private fun dummyLoadWeather(iteration: Int): Map<Long, Double> =
        when (iteration) {
            0 -> emptyMap()
            1 -> mapOf(1L to 20.0)
            2 -> mapOf(1L to 20.0, 2L to 25.0)
            else -> mapOf(1L to 20.0, 2L to 25.0, 3L to 15.0)
        }

    @Test
    fun test() {
        var now: Instant =
            ZonedDateTime
                .of(2023, 12, 12, 16, 54, 48, 0, ZoneOffset.UTC)
                .toInstant()

        var citiesLoadedCount = 0
        var weatherLoadedCount = 0

        val service = WeatherService(
            clock = Clock.fixed(now, ZoneOffset.UTC),
            loadCities = {
                dummyLoadCities(citiesLoadedCount).also {
                    citiesLoadedCount += 1
                }
            },
            loadWeather = {
                dummyLoadWeather(weatherLoadedCount).also {
                    weatherLoadedCount += 1
                }
            }
        )

        // Time is 2023-12-12T16:54:48Z
        // Cities were eagerly loaded but got empty result, it is cached
        // Weather is not loaded at all because it is lazy
        assertNull(service.weatherFor("Amsterdam"))
        assertEquals(1, citiesLoadedCount)
        assertEquals(0, weatherLoadedCount)

        // Time is now 2023-12-13T00:54:48Z
        now = now.plus(8, ChronoUnit.HOURS)

        // TODO: Fix tests

        // Cities were reloaded, now we have Amsterdam and Istanbul, it is cached again
        // Weather is loaded for the first time
        assertEquals(20.0, service.weatherFor("Amsterdam"))
        assertEquals(25.0, service.weatherFor("Amsterdam"))
        assertEquals(2, citiesLoadedCount)
        assertEquals(1, weatherLoadedCount)

    }
}
