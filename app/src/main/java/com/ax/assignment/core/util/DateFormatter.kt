package com.ax.assignment.core.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val fullFormatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일")
private val monthFormatter = DateTimeFormatter.ofPattern("yyyy년 M월")
private val shortFormatter = DateTimeFormatter.ofPattern("M월 d일")

fun LocalDate.toFullDateString(): String = format(fullFormatter)
fun LocalDate.toMonthString(): String = format(monthFormatter)
fun LocalDate.toShortDateString(): String = format(shortFormatter)

/**
 * Period range, split into the Figma day-range text and an optional year tag.
 *
 * The day range always keeps the Figma format ("6월1일 ~ 6월30일") regardless of
 * year, so the layout users see every month never shifts. For periods outside the
 * current year a year tag is returned separately ("2025", or "2025~2026" when the
 * period straddles a year end) so the UI can render it as a quiet trailing badge.
 *
 * @return base day-range to year tag (null when the period is in the current year)
 */
fun periodRangeParts(
    start: LocalDate,
    end: LocalDate,
    today: LocalDate = LocalDate.now(),
): Pair<String, String?> {
    val base = "${start.monthValue}월${start.dayOfMonth}일 ~ ${end.monthValue}월${end.dayOfMonth}일"
    val year = when {
        start.year == today.year && end.year == today.year -> null
        start.year == end.year -> "${start.year}"
        else -> "${start.year}~${end.year}"
    }
    return base to year
}
