package dev.akif.periodik

import org.junit.jupiter.api.Nested
import kotlin.test.assertEquals
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Suppress("MaxLineLength")
class PeriodikTest {
    @Nested
    inner class `a lazy periodik property should` {
        @Test
        fun `not load value before it is accessed for the first time`() {
            val clock = TestClock()
            val logger = TestLogger()

            // Create an object with a lazy periodik property
            Counter.build(
                schedule = 1.seconds,
                loadDelay = 100.milliseconds,
                lazy = true,
                clock = clock,
                logger = logger
            )

            // Expect that nothing is loaded yet
            assertEquals(emptyList(), logger.lines())

            // Wait for a while, long enough for a refresh to happen if it was scheduled
            clock.wait(2.seconds)

            // Expect that still nothing is loaded
            assertEquals(emptyList(), logger.lines())
        }

        @Test
        fun `load value when it is accessed for the first time and schedule refreshes for the value`() {
            val clock = TestClock()
            val logger = TestLogger()

            // Create an object with a lazy periodik property
            val counter = Counter.build(
                schedule = 1.seconds,
                loadDelay = 100.milliseconds,
                lazy = true,
                clock = clock,
                logger = logger
            )

            // Wait for some time before first access
            clock.wait(50.milliseconds)

            // Access the value for the first time
            val value1 = counter.value

            // Expect that the value is loaded with the initial value of counter
            assertEquals(0, value1)

            // Expect that there are logs for loading the value and setting the schedule
            val logsAtFirstAccess = listOf(
                "[L] Initializing Periodik(value)",
                "[L] Updating the value of Periodik(value)",
                "[L] counter is 0",
                "[D] Periodik(value) has a new value at 1970-01-01T00:00:00.150Z: 0",
                "[D] Periodik(value) will be updated again at 1970-01-01T00:00:01.150Z",
                "[D] Reusing last value of Periodik(value) updated at 1970-01-01T00:00:00.150Z",
            )
            assertEquals(logsAtFirstAccess, logger.lines())

            // Access the value a second time and expect the same value since it hasn't been refreshed yet
            val value2 = counter.value
            assertEquals(0, value2)

            // Expect that there are logs for reusing the value
            val logsAtSecondAccess = logsAtFirstAccess + listOf(
                "[D] Reusing last value of Periodik(value) updated at 1970-01-01T00:00:00.150Z"
            )
            assertEquals(logsAtSecondAccess, logger.lines())

            // Wait enough for a refresh to kick in but not enough for it to complete
            clock.wait(950.milliseconds)

            // Access the value before the refresh completes and expect the value to be the same
            val value3 = counter.value
            assertEquals(0, value3)

            // Expect that there are logs for the start of a refresh
            val logsAtThirdAccess = logsAtSecondAccess + listOf(
                "[D] Reusing last value of Periodik(value) updated at 1970-01-01T00:00:00.150Z"
            )
            assertEquals(logsAtThirdAccess, logger.lines())

            // Wait a bit more so the refresh completes
            clock.wait(100.milliseconds)

            // Access the value after the refresh completes and expect the value to be updated
            val value4 = counter.value
            assertEquals(1, value4)

            // Expect that there are logs for completing the refresh
            val logsAtFourthAccess = logsAtThirdAccess + listOf(
                "[L] Value of Periodik(value) is expired at 1970-01-01T00:00:01.150Z",
                "[L] Updating the value of Periodik(value)",
                "[L] counter is 1",
                "[D] Periodik(value) has a new value at 1970-01-01T00:00:01.300Z: 1",
                "[D] Periodik(value) will be updated again at 1970-01-01T00:00:02.300Z",
                "[D] Cancelling the last update job of Periodik(value)"
            )
            assertEquals(logsAtFourthAccess, logger.lines())
        }
    }
}
