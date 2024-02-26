package dev.akif

import dev.akif.periodik.PeriodikBuilder
import dev.akif.periodik.Schedule
import kotlinx.coroutines.Dispatchers
import java.time.Instant

/**
 * Creates a [PeriodikBuilder] with default behaviors and allows for further customizations
 * before building the [Periodik][dev.akif.periodik.Periodik] instance
 *
 * The default behaviors:
 * - getting the current [Instant] by calling [Instant.now]
 * - not making any adjustments to the [Instant]s
 * - using [Dispatchers.IO] for blocking coroutines when needed
 * - waiting for a given [kotlin.time.Duration] by calling [kotlinx.coroutines.delay]
 * - logging messages to [System.out]
 * - logging errors to [System.err] and throwing an [IllegalStateException]
 * - initializing the property eagerly
 *
 * @param Type type of the property
 *
 * @param schedule [Schedule] with which to update the value
 *
 * @return a [PeriodikBuilder] with default behaviors
 */
fun <Type> periodik(schedule: Schedule): PeriodikBuilder<Type> = PeriodikBuilder(schedule)
