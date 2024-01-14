package dev.akif

import dev.akif.periodik.PeriodikBuilder
import kotlinx.coroutines.Dispatchers
import java.time.Instant

fun periodik(): PeriodikBuilder =
    PeriodikBuilder()
        .gettingInstantBy { Instant.now() }
        .adjustingInstantBy { it }
        .blockingOn(Dispatchers.Default)
        .defaultLogging()
        .initializeEagerly()
