package dev.akif.periodik

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.time.Clock
import java.time.Instant
import kotlin.coroutines.CoroutineContext
import kotlin.properties.Delegates.notNull

@Suppress("TooManyFunctions")
class PeriodikBuilder {
    private var schedule by notNull<Schedule>()
    private var currentInstant by notNull<() -> Instant>()
    private var adjustment by notNull<(Instant) -> Instant>()
    private var coroutineContext by notNull<CoroutineContext>()
    private var debug by notNull<(String) -> Unit>()
    private var log by notNull<(String) -> Unit>()
    private var error by notNull<(String) -> Nothing>()
    private var deferInitialization by notNull<Boolean>()

    fun on(schedule: Schedule): PeriodikBuilder =
        apply {
            this.schedule = schedule
        }

    fun gettingInstantBy(currentInstant: () -> Instant): PeriodikBuilder =
        apply {
            this.currentInstant = currentInstant
        }

    fun usingClock(clock: Clock): PeriodikBuilder =
        apply {
            this.currentInstant = { clock.instant() }
        }

    fun adjustingInstantBy(adjustment: (Instant) -> Instant): PeriodikBuilder =
        apply {
            this.adjustment = adjustment
        }

    fun blockingOn(coroutineContext: CoroutineContext): PeriodikBuilder =
        apply {
            this.coroutineContext = coroutineContext
        }

    fun debug(debug: (String) -> Unit): PeriodikBuilder =
        apply {
            this.debug = debug
        }

    fun log(log: (String) -> Unit): PeriodikBuilder =
        apply {
            this.log = log
        }

    fun error(error: (String) -> Nothing): PeriodikBuilder =
        apply {
            this.error = error
        }

    fun defaultLogging(): PeriodikBuilder =
        apply {
            debug { println(it) }
            log { println(it) }
            error { throw IllegalStateException(it) }
        }

    fun initializeEagerly(): PeriodikBuilder =
        apply {
            this.deferInitialization = false
        }

    fun initializeLazily(): PeriodikBuilder =
        apply {
            this.deferInitialization = true
        }

    fun <Type> build(nextValue: (Type?) -> Type): Periodik<Type> {
        val p = Periodik<Type>(schedule, currentInstant, adjustment, coroutineContext, debug, log, error) { oldValue ->
            nextValue(oldValue)
        }

        if (deferInitialization) {
            log("Deferring initialization of $p")
            return p
        }

        log("Eagerly initializing $p")
        runBlocking(coroutineContext) {
            p.get()
        }
        return p
    }

    fun <Type> buildSuspending(
        coroutineContext: CoroutineContext = Dispatchers.IO,
        nextValue: suspend (Type?) -> Type
    ): Periodik<Type> {
        val f: (Type?) -> Type = { oldValue ->
            runBlocking(coroutineContext) {
                nextValue(oldValue)
            }
        }

        return blockingOn(coroutineContext).build(f)
    }
}
