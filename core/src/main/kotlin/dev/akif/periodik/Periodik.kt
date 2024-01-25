package dev.akif.periodik

import kotlinx.coroutines.*
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Periodik is a read-only property delegate that can provide a value refreshed periodically.
 *
 * A quick example that gets a new value every 2 seconds:
 * ```kotlin
 * import dev.akif.periodik
 * import dev.akif.periodik.Schedule
 *
 * val message: String by periodik().on(Schedule.every(2.seconds)).build { previous ->
 *     if (previous == null) {
 *         "Hello world!"
 *     } else {
 *         "Hello again on ${System.currentTimeMillis()}!"
 *     }
 * }
 * ```
 *
 * @param Type
 * the type of the property
 *
 * @param schedule
 * the [Schedule][Schedule] defining the period
 *
 * @param currentInstant
 * the function to get the current [Instant][Instant]
 *
 * @param adjustment
 * the function to adjust the [Instant][Instant]s
 *
 * @param coroutineContext
 * the [CoroutineContext] to use for blocking coroutines
 *
 * @param debug
 * the function to log debug messages
 *
 * @param log
 * the function to log messages
 *
 * @param error
 * the function to log error messages and throw an [Exception][Exception]
 *
 * @param nextValue
 * the function to get the next value, providing the last value as input
 *
 * @see dev.akif.periodik
 * @see PeriodikBuilder
 */
@Suppress("LongParameterList")
class Periodik<out Type> internal constructor(
    private val schedule: Schedule,
    private val currentInstant: () -> Instant,
    private val adjustment: (Instant) -> Instant,
    private val coroutineContext: CoroutineContext,
    private val debug: (String) -> Unit,
    private val log: (String) -> Unit,
    private val error: (String) -> Nothing,
    private val nextValue: (Type?) -> Type
) : ReadOnlyProperty<Any, Type> {
    private val name: AtomicReference<String?> = AtomicReference(null)
    private val lastValue: AtomicReference<Type?> = AtomicReference(null)
    private val lastGetInstant: AtomicReference<Instant?> = AtomicReference(null)

    /** @inheritDoc */
    override fun getValue(thisRef: Any, property: KProperty<*>): Type {
        name.compareAndSet(null, property.name)
        return runBlocking(coroutineContext) { getIfNeeded() }
    }

    private suspend fun getIfNeeded(): Type {
        if (canReuseLastValue()) {
            return lastValue.get() ?: error("Value should not be null here")
        }
        return get()
    }

    @OptIn(DelicateCoroutinesApi::class)
    internal suspend fun get(): Type {
        log("Getting a new value of $this")

        val instant = currentInstant()
        val value = nextValue(lastValue.get())
        lastValue.set(value)
        lastGetInstant.set(instant)
        debug("$this has a new value at $instant: $value")

        val nextGetInstant = adjustment(schedule.nextOccurrence(instant))
        GlobalScope.async {
            debug("${this@Periodik} will be get again at $nextGetInstant")
            delay(nextGetInstant.toEpochMilli() - instant.toEpochMilli())
            get()
        }

        return value
    }

    @Suppress("ReturnCount")
    private fun canReuseLastValue(): Boolean {
        if (lastValue.get() == null || lastGetInstant.get() == null) {
            return false
        }

        val lastInstant = lastGetInstant.get() ?: error("Value should not be null here")
        val nextInstant = adjustment(schedule.nextOccurrence(lastInstant))
        val isInFuture = currentInstant().toEpochMilli() < nextInstant.toEpochMilli()

        return if (isInFuture) {
            debug("Reusing last value of $this for lastInstant $lastInstant and nextInstant $nextInstant")
            true
        } else {
            log("Value of $this is expired")
            false
        }
    }

    /** @inheritDoc */
    override fun toString(): String =
        "Periodik(${name.get() ?: "schedule=$schedule"})"
}
