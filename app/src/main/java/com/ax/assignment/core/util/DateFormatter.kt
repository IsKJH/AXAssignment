package com.ax.assignment.core.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val fullFormatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일")
private val monthFormatter = DateTimeFormatter.ofPattern("yyyy년 M월")
private val shortFormatter = DateTimeFormatter.ofPattern("M월 d일")

fun LocalDate.toFullDateString(): String = format(fullFormatter)
fun LocalDate.toMonthString(): String = format(monthFormatter)
fun LocalDate.toShortDateString(): String = format(shortFormatter)
