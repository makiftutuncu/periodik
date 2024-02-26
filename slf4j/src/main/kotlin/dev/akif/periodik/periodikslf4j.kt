package dev.akif.periodik

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Sets logging functions to use SLF4J
 *
 * @param Type type of the property
 *
 * @receiver the [PeriodikBuilder] instance
 *
 * @param logger SLF4J logger to use, with `dev.akif.periodik.Periodik` as the default
 *
 * @return the [PeriodikBuilder] instance so further customizations can be made
 */
fun <Type> PeriodikBuilder<Type>.logBySlf4j(
    logger: Logger = LoggerFactory.getLogger(Periodik::class.java)
): PeriodikBuilder<Type> =
    apply {
        debugBy { logger.debug(it) }
        logBy { logger.info(it) }
        errorBy {
            logger.error(it)
            throw IllegalStateException(it)
        }
    }
