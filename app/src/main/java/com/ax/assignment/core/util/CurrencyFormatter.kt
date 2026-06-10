package com.ax.assignment.core.util

import java.text.NumberFormat
import java.util.Locale

private val krwFormat = NumberFormat.getNumberInstance(Locale.KOREA)

fun Long.toCurrencyString(): String = "${krwFormat.format(this)}원"
