package dev.akif.periodik

import kotlin.test.fail

class TestLogger {
    private val lines: MutableList<String> = mutableListOf()

    fun debug(line: String) {
        val log = "[D] $line"
        println(log)
        lines += log
    }

    fun log(line: String) {
        val log = "[L] $line"
        println(log)
        lines += log
    }

    fun lines(): List<String> = lines.toList()
}

fun <Type> PeriodikBuilder<Type>.usingTestLogger(logger: TestLogger): PeriodikBuilder<Type> =
    debugBy { logger.debug(it) }
        .logBy { logger.log(it) }
        .errorBy { fail(it) }
