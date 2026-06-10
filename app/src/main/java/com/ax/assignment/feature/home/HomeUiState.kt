package com.ax.assignment.feature.home

import com.ax.assignment.domain.model.Transaction
import java.time.LocalDate

data class HomeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val transactions: List<Transaction> = emptyList(),
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val selectedYear: Int = LocalDate.now().year,
    val selectedMonth: Int = LocalDate.now().monthValue,
    val periodStart: LocalDate = LocalDate.of(selectedYear, selectedMonth, 1),
    val periodEnd: LocalDate = periodStart.withDayOfMonth(periodStart.lengthOfMonth()),
) {
    val balance: Long get() = totalIncome - totalExpense
    val isEmpty: Boolean get() = !isLoading && transactions.isEmpty()
    val isExceeded: Boolean get() = totalExpense > totalIncome && (totalIncome > 0 || totalExpense > 0)
    val budgetRatio: Float get() = when {
        totalIncome > 0 -> totalExpense.toFloat() / totalIncome.toFloat()
        totalExpense > 0 -> 1f
        else -> 0f
    }
}

sealed class HomeEvent {
    object PrevMonth : HomeEvent()
    object NextMonth : HomeEvent()
}
