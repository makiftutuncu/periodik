package dev.akif.exchangerates

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class CurrencyRepository {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(CurrencyRepository::class.java)
    }

    fun all(): Set<String> {
        log.info("Loading currencies")
        return setOf("USD", "EUR", "TRY", "GBP")
    }
}
