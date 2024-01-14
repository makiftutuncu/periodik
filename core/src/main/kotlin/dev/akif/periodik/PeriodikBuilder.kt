package dev.akif.periodik

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.time.Clock
import java.time.Instant
import kotlin.coroutines.CoroutineContext
import kotlin.properties.Delegates.notNull

/**
 * A builder for [Periodik][Periodik] instances
 */
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

    /**
     * Sets the [Schedule][Schedule] to use in the [Periodik][Periodik] instance to build
     *
     * @return
     * the [Periodik][Periodik] instance so further customizations can be made
     */
    fun on(schedule: Schedule): PeriodikBuilder =
        apply {
            this.schedule = schedule
        }

    /**
     * Sets the function to use for getting the current [Instant][Instant]
     * in the [Periodik][Periodik] instance to build
     *
     * @return
     * the [Periodik][Periodik] instance so further customizations can be made
     */
    fun gettingInstantBy(currentInstant: () -> Instant): PeriodikBuilder =
        apply {
            this.currentInstant = currentInstant
        }

    /**
     * Sets the [Clock] to use for getting the current [Instant][Instant]
     * in the [Periodik][Periodik] instance to build
     *
     * @return
     * the [Periodik][Periodik] instance so further customizations can be made
     */
    fun usingClock(clock: Clock): PeriodikBuilder =
        apply {
            this.currentInstant = { clock.instant() }
        }

    /**
     * Sets the function to use for adjusting the [Instant][Instant]s
     * in the [Periodik][Periodik] instance to build
     *
     * @return
     * the [Periodik][Periodik] instance so further customizations can be made
     */
    fun adjustingInstantBy(adjustment: (Instant) -> Instant): PeriodikBuilder =
        apply {
            this.adjustment = adjustment
        }

    /**
     * Sets the [CoroutineContext] to use for blocking coroutines
     * in the [Periodik][Periodik] instance to build
     *
     * @return
     * the [Periodik][Periodik] instance so further customizations can be made
     */
    fun blockingOn(coroutineContext: CoroutineContext): PeriodikBuilder =
        apply {
            this.coroutineContext = coroutineContext
        }

    /**
     * Sets the function to use for logging debug messages
     * in the [Periodik][Periodik] instance to build
     *
     * @return
     * the [Periodik][Periodik] instance so further customizations can be made
     */
    fun debug(debug: (String) -> Unit): PeriodikBuilder =
        apply {
            this.debug = debug
        }

    /**
     * Sets the function to use for logging messages
     * in the [Periodik][Periodik] instance to build
     *
     * @return
     * the [Periodik][Periodik] instance so further customizations can be made
     */
    fun log(log: (String) -> Unit): PeriodikBuilder =
        apply {
            this.log = log
        }

    /**
     * Sets the function to use for logging error messages
     * in the [Periodik][Periodik] instance to build
     *
     * Error function returns [Nothing] to make sure that
     * an [Exception] is thrown
     *
     * @return
     * the [Periodik][Periodik] instance so further customizations can be made
     */
    fun error(error: (String) -> Nothing): PeriodikBuilder =
        apply {
            this.error = error
        }

    /**
     * Sets logging functions in the [Periodik][Periodik] instance
     * to build to use default logging which logs to [System.out]
     * and throws [IllegalStateException] on errors
     *
     * @return
     * the [Periodik][Periodik] instance so further customizations can be made
     */
    fun defaultLogging(): PeriodikBuilder =
        apply {
            debug { println(it) }
            log { println(it) }
            error { throw IllegalStateException(it) }
        }

    /**
     * Sets the [Periodik][Periodik] instance to build to initialize eagerly
     *
     * @return
     * the [Periodik][Periodik] instance so further customizations can be made
     */
    fun initializeEagerly(): PeriodikBuilder =
        apply {
            this.deferInitialization = false
        }

    /**
     * Sets the [Periodik][Periodik] instance to build to initialize lazily
     *
     * @return
     * the [Periodik][Periodik] instance so further customizations can be made
     */
    fun initializeLazily(): PeriodikBuilder =
        apply {
            this.deferInitialization = true
        }

    /**
     * Builds a [Periodik][Periodik] instance with the given [nextValue] function
     *
     * @param nextValue
     * function to use for getting the next value, providing the last value as input
     *
     * @return
     * A [Periodik][Periodik] instance built with all the customizations made
     */
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

    /**
     * Builds a [Periodik][Periodik] instance with the given [nextValue] function
     * which is a suspending function
     *
     * @param coroutineContext
     * [CoroutineContext] to use for blocking coroutines
     *
     * @param nextValue
     * function to use for getting the next value, providing the last value as input
     *
     * @return
     * A [Periodik][Periodik] instance built with all the customizations made
     */
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
