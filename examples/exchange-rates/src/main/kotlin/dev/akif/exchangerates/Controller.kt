package dev.akif.exchangerates

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class Controller(private val rates: Service) {
    @GetMapping("/exchange-rates/{from}/{to}")
    suspend fun get(@PathVariable from: String, @PathVariable to: String): ExchangeRate =
        rates.get(from, to) ?: throw ExchangeRateNotFoundException(from, to)

    class ExchangeRateNotFoundException(from: String, to: String) : ResponseStatusException(
        HttpStatus.NOT_FOUND,
        "Exchange rate $from/$to is not found"
    )
}
