package dev.akif.exchangerates

import kotlinx.coroutines.delay
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

@Component
class RateApi {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(RateApi::class.java)

        private const val lower: Double = 0.2
        private const val upper: Double = 50.0
    }

    suspend fun ratesFor(currency: String): Map<String, Double> {
        log.info("Getting rates for {}", currency)
        delay(300.milliseconds)
        return mapOf(
            "EUR" to Random.nextDouble(lower, upper),
            "USD" to Random.nextDouble(lower, upper),
            "TRY" to Random.nextDouble(lower, upper)
        )
    }
}
