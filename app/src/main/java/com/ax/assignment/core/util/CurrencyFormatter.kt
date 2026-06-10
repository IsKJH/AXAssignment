package com.ax.assignment.core.util

import java.text.NumberFormat
import java.util.Locale

private val krwFormat = NumberFormat.getNumberInstance(Locale.KOREA)

fun Long.toCurrencyString(): String = "${krwFormat.format(this)}원"

fun Long.toSignedCurrencyString(isIncome: Boolean): String {
    val prefix = if (isIncome) "+" else "-"
    return "$prefix${krwFormat.format(this)}원"
}

fun Long.toManUnitString(): String = if (this < 10_000) toCurrencyString() else "${this / 10_000}만"
