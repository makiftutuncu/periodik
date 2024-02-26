package dev.akif.periodik

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.time.Duration

class TestClock(private var adjustment: Duration = Duration.ZERO) : Clock() {
    val now: Instant
        get() = Instant.EPOCH.plusMillis(adjustment.inWholeMilliseconds)

    override fun instant(): Instant = now

    override fun withZone(zone: ZoneId?): Clock = this

    override fun getZone(): ZoneId = ZoneOffset.UTC

    fun wait(duration: Duration) {
        adjustment += duration
    }
}
