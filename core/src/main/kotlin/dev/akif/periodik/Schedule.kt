package dev.akif.periodik

import java.time.Instant
import kotlin.time.Duration

/**
 * A schedule that determines when the next occurrence will be,
 * taking the last occurrence into account
 */
interface Schedule {
    /**
     * Calculates the next occurrence based on the last occurrence
     *
     * @param lastOccurrence
     * the last occurrence [Instant][Instant]
     *
     * @return
     * the next occurrence [Instant][Instant]
     */
    fun nextOccurrence(lastOccurrence: Instant): Instant

    /** @suppress */
    companion object {
        /**
         * Creates an [Every][Every] [Schedule][Schedule]
         *
         * @param duration
         * the duration of [Every][Every] [Schedule][Schedule]
         *
         * @return
         * an [Every][Every] [Schedule][Schedule]
         */
        fun every(duration: Duration): Schedule =
            Every(duration)
    }

    /**
     * A [Schedule] that will occur every [duration]
     *
     * @property duration
     * the duration between occurrences
     */
    data class Every(val duration: Duration) : Schedule {
        override fun nextOccurrence(lastOccurrence: Instant): Instant =
            lastOccurrence.plusMillis(duration.inWholeMilliseconds)
    }
}
