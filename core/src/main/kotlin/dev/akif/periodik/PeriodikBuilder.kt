package dev.akif.periodik

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.time.Clock
import java.time.Instant
import kotlin.time.Duration

/**
 * A builder for [Periodik] instances with default behaviors and allowing for customizations
 *
 * Such customization functions have the [Periodik] instance as receiver (`this` reference) so that
 * members of [Periodik] can be accessed within the passed function
 *
 * @param Type type of the property
 *
 * @param schedule [Schedule] with which to update the value
 * @param currentInstant function to use for getting the current [Instant]
 * @param adjustment function to use for adjusting the [Instant]s before using them in time calculations
 * @param dispatcher [CoroutineDispatcher] to use for blocking coroutines when needed
 * @param wait function to use for waiting for a given [kotlin.time.Duration]
 * @param debug function to use for logging debug messages
 * @param log function to use for logging messages
 * @param error function to use for logging error messages and throwing an [Exception]
 * @param deferInitialization whether to initialize the property eagerly or lazily
 */
@Suppress("TooManyFunctions", "LongParameterList")
class PeriodikBuilder<Type>(
    private val schedule: Schedule,
    private var currentInstant: Periodik<Type>.() -> Instant = { Instant.now() },
    private var adjustment: Periodik<Type>.(Instant) -> Instant = { it },
    private var dispatcher: CoroutineDispatcher = Periodik.defaultDispatcher,
    private var wait: suspend Periodik<Type>.(Duration) -> Unit = { delay(it) },
    private var debug: Periodik<Type>.(String) -> Unit = { println(it) },
    private var log: Periodik<Type>.(String) -> Unit = { println(it) },
    private var error: Periodik<Type>.(String) -> Nothing = { System.err.println(it); throw IllegalStateException(it) },
    private var deferInitialization: Boolean = false
) {
    /**
     * Sets the [currentInstant]
     *
     * @param newCurrentInstant new value of the [currentInstant]
     *
     * @return the [PeriodikBuilder] instance so further customizations can be made
     */
    fun currentInstantBy(newCurrentInstant: Periodik<Type>.() -> Instant): PeriodikBuilder<Type> =
        apply {
            this.currentInstant = newCurrentInstant
        }

    /**
     * Sets [currentInstant] by using provided [Clock] so that the current [Instant] is retrieved from it
     *
     * @param clock [Clock] to use for getting the current [Instant]
     *
     * @return the [PeriodikBuilder] instance so further customizations can be made
     */
    fun currentInstantFrom(clock: Clock): PeriodikBuilder<Type> =
        apply {
            this.currentInstant = { clock.instant() }
        }

    /**
     * Sets the [adjustment]
     *
     * @param newAdjustment new value of the [adjustment]
     *
     * @return the [PeriodikBuilder] instance so further customizations can be made
     */
    fun adjustmentBy(newAdjustment: Periodik<Type>.(Instant) -> Instant): PeriodikBuilder<Type> =
        apply {
            this.adjustment = newAdjustment
        }

    /**
     * Sets the [dispatcher]
     *
     * **Please make sure the [CoroutineDispatcher] you provide has spare threads.
     * Otherwise, you risk blocking your application.**
     * This is a limitation of property delegation in Kotlin
     * because [getValue][kotlin.properties.ReadOnlyProperty.getValue] is not a suspending function.
     * [Periodik] uses [runBlocking] with the dispatcher to be able to use suspending functions
     * in the [nextValue][Periodik.nextValue] function.
     *
     * @param newDispatcher new value of the [dispatcher]
     *
     * @return the [PeriodikBuilder] instance so further customizations can be made
     */
    fun dispatchTo(newDispatcher: CoroutineDispatcher): PeriodikBuilder<Type> =
        apply {
            this.dispatcher = newDispatcher
        }

    /**
     * Sets the [wait]
     *
     * @param newWait new value of the [wait]
     *
     * @return the [PeriodikBuilder] instance so further customizations can be made
     */
    fun waitBy(newWait: suspend Periodik<Type>.(Duration) -> Unit): PeriodikBuilder<Type> =
        apply {
            this.wait = newWait
        }

    /**
     * Sets the [debug]
     *
     * @param newDebug new value of the [debug]
     *
     * @return the [PeriodikBuilder] instance so further customizations can be made
     */
    fun debugBy(newDebug: Periodik<Type>.(String) -> Unit): PeriodikBuilder<Type> =
        apply {
            this.debug = newDebug
        }

    /**
     * Sets the [log]
     *
     * @param newLog new value of the [log]
     *
     * @return the [PeriodikBuilder] instance so further customizations can be made
     */
    fun logBy(newLog: Periodik<Type>.(String) -> Unit): PeriodikBuilder<Type> =
        apply {
            this.log = newLog
        }

    /**
     * Sets the [error]
     *
     * Error function returns [Nothing] to make sure that
     * an [Exception] is thrown
     *
     * @param newError new value of the [error]
     *
     * @return the [PeriodikBuilder] instance so further customizations can be made
     */
    fun errorBy(newError: Periodik<Type>.(String) -> Nothing): PeriodikBuilder<Type> =
        apply {
            this.error = newError
        }

    /**
     * Sets the [deferInitialization]
     *
     * @return the [PeriodikBuilder] instance so further customizations can be made
     */
    fun deferInitialization(newDeferInitialization: Boolean): PeriodikBuilder<Type> =
        apply {
            this.deferInitialization = newDeferInitialization
        }

    /**
     * Sets the [deferInitialization] to false so property initializes eagerly
     *
     * @return the [PeriodikBuilder] instance so further customizations can be made
     */
    fun initializeEagerly(): PeriodikBuilder<Type> =
        deferInitialization(false)

    /**
     * Sets the [deferInitialization] to true so property initializes lazily
     *
     * @return the [PeriodikBuilder] instance so further customizations can be made
     */
    fun initializeLazily(): PeriodikBuilder<Type> =
        deferInitialization(true)

    /**
     * Builds a [Periodik] instance with the given [nextValue] function
     *
     * @param nextValue function to use for getting the next value with the last known value
     * as function input so that it can be used in calculation of the next value
     *
     * @return A [Periodik] instance built with all the customizations made
     */
    fun build(nextValue: Periodik<Type>.(Type?) -> Type): Periodik<Type> =
        Periodik(
            schedule,
            currentInstant,
            adjustment,
            dispatcher,
            wait,
            debug,
            log,
            error,
            deferInitialization
        ) { oldValue ->
            nextValue(oldValue)
        }

    /**
     * Builds a [Periodik] instance with the given [nextValue] function
     * which is a suspending function
     *
     * @param nextValue function to use for getting the next value with the last known value
     * as function input so that it can be used in calculation of the next value
     *
     * @return A [Periodik] instance built with all the customizations made
     */
    fun buildSuspending(
        nextValue: suspend Periodik<Type>.(Type?) -> Type
    ): Periodik<Type> =
        build { oldValue ->
            runBlocking(dispatcher) {
                nextValue(oldValue)
            }
        }
}
