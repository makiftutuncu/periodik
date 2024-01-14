package dev.akif.periodik

import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun PeriodikBuilder.loggingWithSlf4j(logger: Logger = LoggerFactory.getLogger(Periodik::class.java)): PeriodikBuilder =
    apply {
        debug { logger.debug(it) }
        log { logger.info(it) }
        error {
            logger.error(it)
            throw IllegalStateException(it)
        }
    }
