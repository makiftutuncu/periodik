package dev.akif.exchangerates

import dev.akif.periodik.Periodik
import dev.akif.periodik.Schedule
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds
import org.springframework.stereotype.Service as ServiceAnnotation

@ServiceAnnotation
class Service(
    private val currencyRepository: CurrencyRepository,
    private val rateApi: RateApi
) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(Service::class.java)
    }

    private val currencies by Periodik.default(schedule = Schedule.every(1.days)) {
        currencyRepository.all()
    }

    private val rates by Periodik.suspending(
        schedule = Schedule.every(5.seconds),
        deferInitialization = true
    ) {
        val supportedCurrencies = currencies
        val ratesForAll = coroutineScope {
            supportedCurrencies.map { c ->
                async {
                    val ratesForCurrency = rateApi.ratesFor(c).filterKeys { supportedCurrencies.contains(it) }
                    c to ratesForCurrency
                }
            }
        }.awaitAll()

        ratesForAll.associateBy(
            keySelector = { (currency, _) -> currency },
            valueTransform = { (_, ratesForCurrency) -> ratesForCurrency }
        )
    }

    suspend fun get(from: String, to: String): ExchangeRate? =
        when {
            !isValidCurrency(from) || !isValidCurrency(to) -> null
            from == to -> ExchangeRate(from, to, 1.0)
            else -> {
                log.info("Getting exchange rate from $from to $to")
                rates[from]?.let { it[to] }?.let { ExchangeRate(from, to, it) }
            }
        }

    private fun isValidCurrency(currency: String): Boolean {
        if (!currencies.contains(currency)) {
            log.warn("Currency $currency is not valid")
            return false
        }
        return true
    }
}
