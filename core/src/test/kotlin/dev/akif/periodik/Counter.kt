package dev.akif.periodik

import dev.akif.periodik
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration

interface Counter {
    val value: Int

    companion object {
        @Suppress("LongParameterList")
        fun build(
            schedule: Duration,
            loadDelay: Duration,
            lazy: Boolean,
            clock: TestClock,
            logger: TestLogger
        ): Counter =
            object : Counter {
                private val delegate: Periodik<Int> = periodik<Int>(Schedule.every(schedule))
                    .deferInitialization(lazy)
                    .currentInstantFrom(clock)
                    .usingTestLogger(logger)
                    .waitBy { duration ->
                        Thread.sleep(duration.inWholeMilliseconds)
                        clock.wait(duration)
                    }
                    .build { old ->
                        runBlocking(dispatcher) { wait(loadDelay) }
                        val new = old?.plus(1) ?: 0
                        log("counter is $new")
                        new
                    }

                override val value by delegate
            }
    }
}
