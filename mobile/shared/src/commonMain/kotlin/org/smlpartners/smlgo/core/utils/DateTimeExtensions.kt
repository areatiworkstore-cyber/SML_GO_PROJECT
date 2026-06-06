package org.smlpartners.smlgo.core.utils

import kotlin.time.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

fun today(): LocalDate =
    Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date

fun LocalDate.plusDays(days: Int): LocalDate =
    plus(DatePeriod(days = days))

fun LocalDate.minusDays(days: Int): LocalDate =
    minus(DatePeriod(days = days))