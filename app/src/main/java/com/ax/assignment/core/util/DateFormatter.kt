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
 * Period range label, e.g. "6월1일 ~ 6월30일" (Figma 기준 표기).
 * Years other than the current one are prefixed ("2025년 12월1일 ~ …")
 * so past/future periods stay unambiguous.
 */
fun periodRangeLabel(start: LocalDate, end: LocalDate, today: LocalDate = LocalDate.now()): String {
    val startStr = buildString {
        if (start.year != today.year) append("${start.year}년 ")
        append("${start.monthValue}월${start.dayOfMonth}일")
    }
    val endStr = buildString {
        if (end.year != start.year) append("${end.year}년 ")
        append("${end.monthValue}월${end.dayOfMonth}일")
    }
    return "$startStr ~ $endStr"
}
