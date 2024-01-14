package dev.akif.periodik

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Sets logging functions to use SLF4J
 *
 * @param logger
 * SLF4J logger to use
 *
 * @return
 * the [Periodik][Periodik] instance so further customizations can be made
 */
fun PeriodikBuilder.loggingWithSlf4j(logger: Logger = LoggerFactory.getLogger(Periodik::class.java)): PeriodikBuilder =
    apply {
        debug { logger.debug(it) }
        log { logger.info(it) }
        error {
            logger.error(it)
            throw IllegalStateException(it)
        }
    }
