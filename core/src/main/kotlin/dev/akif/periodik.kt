package dev.akif

import dev.akif.periodik.PeriodikBuilder
import kotlinx.coroutines.Dispatchers
import java.time.Instant

/**
 * Creates a [PeriodikBuilder][PeriodikBuilder]
 * with default values so that further customizations can be made
 * before building the [Periodik][Periodik] instance
 *
 * @return
 * a [PeriodikBuilder] that will
 * * get current [Instant][Instant] by calling [Instant.now]
 * * not make any adjustments to the [Instant][Instant]s
 * * use [Dispatchers.Default] for blocking coroutines
 * * use default logging which logs to [System.out] and throws [IllegalStateException] on errors
 * * initializes the property eagerly
 */
fun periodik(): PeriodikBuilder =
    PeriodikBuilder()
        .gettingInstantBy { Instant.now() }
        .adjustingInstantBy { it }
        .blockingOn(Dispatchers.Default)
        .defaultLogging()
        .initializeEagerly()
