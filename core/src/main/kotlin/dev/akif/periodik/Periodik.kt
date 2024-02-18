package dev.akif.periodik

import kotlinx.coroutines.*
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.time.Duration.Companion.milliseconds

/**
 * Periodik is a read-only property delegate that can provide a value updated periodically.
 *
 * A quick example that gets and logs a new value every 2 seconds:
 * ```kotlin
 * import dev.akif.periodik
 * import dev.akif.periodik.Schedule
 *
 * val message by periodik<String>(Schedule.every(2.seconds)).build { previous ->
 *     val newValue = if (previous == null) {
 *         "Hello world!"
 *     } else {
 *         "Hello again on ${System.currentTimeMillis()}!"
 *     }
 *     this.log(newValue)
 *     newValue
 * }
 * ```
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
 * @param nextValue function to use for getting the next value with the last known value
 * as function input so that it can be used in calculation of the next value
 *
 * @see dev.akif.periodik
 * @see PeriodikBuilder
 */
@Suppress("LongParameterList")
class Periodik<Type> internal constructor(
    val schedule: Schedule,
    val currentInstant: Periodik<Type>.() -> Instant,
    val adjustment: Periodik<Type>.(Instant) -> Instant,
    val dispatcher: CoroutineDispatcher,
    val wait: suspend Periodik<Type>.(kotlin.time.Duration) -> Unit,
    val debug: Periodik<Type>.(String) -> Unit,
    val log: Periodik<Type>.(String) -> Unit,
    val error: Periodik<Type>.(String) -> Nothing,
    deferInitialization: Boolean,
    private val nextValue: suspend Periodik<Type>.(Type?) -> Type
) : ReadOnlyProperty<Any, Type> {
    /** @suppress */
    companion object {
        /**
         * Default [CoroutineDispatcher] to use when initializing a [Periodik]
         */
        val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO
    }

    private val name: AtomicReference<String?> = AtomicReference(null)
    private val lastValue: AtomicReference<Type?> = AtomicReference(null)
    private val lastUpdateInstant: AtomicReference<Instant?> = AtomicReference(null)
    private val lastUpdateJob: AtomicReference<Job?> = AtomicReference(null)

    private val scope: CoroutineScope = CoroutineScope(dispatcher)

    init {
        if (!deferInitialization) {
            log("Eagerly initializing Periodik(schedule=$schedule)")
            updateValue(defaultDispatcher)
        }
    }

    /** @inheritDoc */
    override fun getValue(thisRef: Any, property: KProperty<*>): Type {
        if (name.compareAndSet(null, property.name)) {
            log("Initializing $this")
            updateValue(defaultDispatcher)
        }

        if (!canReuseLastValue()) {
            return updateValue(dispatcher)
        }

        return getLastValue()
    }

    private fun updateValue(dispatcher: CoroutineDispatcher): Type {
        log("Updating the value of $this")
        val value = runBlocking(dispatcher) {
            nextValue(lastValue.get())
        }
        lastValue.set(value)
        val instant = currentInstant()
        lastUpdateInstant.set(instant)
        debug("$this has a new value at $instant: $value")

        scheduleNextUpdate()

        return value
    }

    private fun scheduleNextUpdate() {
        val (lastInstant, nextInstant) = getLastAndNextUpdateTime()
        val delayDuration = (nextInstant.toEpochMilli() - lastInstant.toEpochMilli()).milliseconds
        debug("$this will be updated again at $nextInstant")

        lastUpdateJob.getAndUpdate { lastJob ->
            lastJob?.also {
                debug("Cancelling the last update job of $this")
                it.cancel()
            }
            scope.launch(dispatcher) {
                wait(delayDuration)
                updateValue(dispatcher)
            }
        }
    }

    @Suppress("ReturnCount")
    private fun canReuseLastValue(): Boolean {
        if (lastValue.get() == null) {
            return false
        }

        val (lastInstant, nextInstant) = getLastAndNextUpdateTime()
        val now = currentInstant()

        return if (now.toEpochMilli() <= nextInstant.toEpochMilli()) {
            debug("Reusing last value of $this updated at $lastInstant")
            true
        } else {
            log("Value of $this is expired at $nextInstant")
            false
        }
    }

    private fun getLastValue(): Type =
        lastValue.get() ?: error("Value should not be null here")

    private fun getLastAndNextUpdateTime(): Pair<Instant, Instant> {
        val lastInstant = lastUpdateInstant.get() ?: error("Value should not be null here")
        val nextInstant = adjustment(schedule.nextOccurrence(lastInstant))
        return lastInstant to nextInstant
    }

    /** @inheritDoc */
    override fun toString(): String =
        "Periodik(${name.get() ?: "schedule=$schedule"})"
}
