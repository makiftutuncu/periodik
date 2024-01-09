package dev.akif.exchangerates

data class ExchangeRate(
    val from: String,
    val to: String,
    val rate: Double
)
