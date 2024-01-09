package dev.akif.periodik

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class Periodik<out Type> private constructor(
    private val schedule: Schedule,
    private val adjustment: (Instant) -> Instant,
    private val currentInstant: () -> Instant,
    private val coroutineContext: CoroutineContext,
    private val getter: () -> Type
) : ReadOnlyProperty<Any, Type> {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(Periodik::class.java)

        fun <Type> default(
            schedule: Schedule,
            deferInitialization: Boolean = false,
            adjustment: (Instant) -> Instant = { it },
            currentInstant: () -> Instant = { Instant.now() },
            getter: () -> Type
        ): Periodik<Type> {
            val periodik = Periodik(schedule, adjustment, currentInstant, Dispatchers.Default) { getter() }
            if (deferInitialization) {
                debug { "Deferring initialization of $periodik" }
            } else {
                debug { "Eagerly initializing $periodik" }
                runBlocking(periodik.coroutineContext) {
                    periodik.get()
                }
            }
            return periodik
        }

        @Suppress("LongParameterList")
        fun <Type> suspending(
            schedule: Schedule,
            coroutineContext: CoroutineContext = Dispatchers.IO,
            deferInitialization: Boolean = false,
            adjustment: (Instant) -> Instant = { it },
            currentInstant: () -> Instant = { Instant.now() },
            getter: suspend () -> Type
        ): Periodik<Type> =
            default(schedule, deferInitialization, adjustment, currentInstant) {
                runBlocking(coroutineContext) { getter() }
            }

        private inline fun debug(message: () -> String) {
            if (log.isDebugEnabled) {
                log.debug(message())
            }
        }

        private inline fun trace(message: () -> String) {
            if (log.isTraceEnabled) {
                log.trace(message())
            }
        }
    }

    private val name: AtomicReference<String?> = AtomicReference(null)
    private val lastValue: AtomicReference<Type?> = AtomicReference(null)
    private val lastGetInstant: AtomicReference<Instant?> = AtomicReference(null)

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
    private suspend fun get(): Type {
        debug { "Getting a new value of $this" }
        val instant = currentInstant()
        val value = getter()
        lastValue.set(value)
        lastGetInstant.set(instant)
        trace { "$this has a new value at $instant: $value" }
        val nextGetInstant = adjustment(schedule.nextOccurrence(instant))
        GlobalScope.async {
            trace { "${this@Periodik} will be get again at $nextGetInstant" }
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
        if (isInFuture) {
            trace { "Reusing last value of $this for lastInstant $lastInstant and nextInstant $nextInstant" }
            return true
        }
        debug {
            "Value of $this is expired"
        }
        return false
    }

    override fun toString(): String =
        "Periodik(${name.get() ?: "schedule=$schedule"})"
}
