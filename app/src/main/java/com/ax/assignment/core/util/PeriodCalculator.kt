package com.ax.assignment.core.util

import java.time.LocalDate

data class PeriodRange(
    val start: LocalDate,
    val end: LocalDate,
)

fun currentPeriodStart(today: LocalDate, startDay: Int): LocalDate {
    val normalizedStartDay = startDay.coerceIn(1, 28)
    val currentMonthStart = LocalDate.of(today.year, today.monthValue, normalizedStartDay)
    return if (today.dayOfMonth >= normalizedStartDay) {
        currentMonthStart
    } else {
        currentMonthStart.minusMonths(1)
    }
}

fun periodRange(year: Int, month: Int, startDay: Int): PeriodRange {
    val start = LocalDate.of(year, month, startDay.coerceIn(1, 28))
    return PeriodRange(start = start, end = start.plusMonths(1).minusDays(1))
}
