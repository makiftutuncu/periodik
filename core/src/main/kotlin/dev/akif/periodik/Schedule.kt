package dev.akif.periodik

import java.time.Instant
import kotlin.time.Duration

interface Schedule {
    fun nextOccurrence(lastOccurrence: Instant): Instant

    companion object {
        fun every(duration: Duration): Schedule =
            Every(duration)
    }

    data class Every(val duration: Duration) : Schedule {
        override fun nextOccurrence(lastOccurrence: Instant): Instant =
            lastOccurrence.plusMillis(duration.inWholeMilliseconds)
    }
}
