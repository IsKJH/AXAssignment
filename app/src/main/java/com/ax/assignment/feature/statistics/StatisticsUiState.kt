package com.ax.assignment.feature.statistics

import com.ax.assignment.domain.model.CategorySummary
import java.time.LocalDate

data class MonthlyExpense(val year: Int, val month: Int, val total: Long)

data class CategoryComparison(
    val summary: CategorySummary,
    val prevAmount: Long,
    val percentage: Float,
)

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedYear: Int = LocalDate.now().year,
    val selectedMonth: Int = LocalDate.now().monthValue,
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val categoryTotals: List<CategorySummary> = emptyList(),
    val prevCategoryTotals: List<CategorySummary> = emptyList(),
    val monthlyExpenseTrend: List<MonthlyExpense> = emptyList(),
    val periodStart: LocalDate = LocalDate.of(selectedYear, selectedMonth, 1),
    val periodEnd: LocalDate = periodStart.withDayOfMonth(periodStart.lengthOfMonth()),
) {
    val balance: Long get() = totalIncome - totalExpense
    val isEmptyExpense: Boolean get() = totalExpense == 0L

    val categoryComparisons: List<CategoryComparison>
        get() {
            val sorted = categoryTotals
                .filter { it.totalAmount > 0 }
                .sortedWith(
                    compareByDescending<CategorySummary> { it.totalAmount }
                        .thenBy { it.category?.sortOrder ?: Int.MAX_VALUE },
                )
            return sorted.map { summary ->
                val prevAmount = prevCategoryTotals
                    .find { it.category?.id == summary.category?.id }
                    ?.totalAmount ?: 0L
                CategoryComparison(
                    summary = summary,
                    prevAmount = prevAmount,
                    percentage = if (totalExpense == 0L) 0f
                    else summary.totalAmount.toFloat() / totalExpense,
                )
            }
        }

    val topCategories: List<CategorySummary>
        get() {
            val active = categoryTotals.filter { it.totalAmount > 0 }
            if (active.isEmpty()) return emptyList()
            val maxAmount = active.maxOf { it.totalAmount }
            return active.filter { it.totalAmount == maxAmount }
        }

    val donutSegments: List<Pair<CategorySummary, Float>>
        get() = categoryComparisons.map { it.summary to it.percentage }
}

sealed class StatisticsEvent {
    object PrevMonth : StatisticsEvent()
    object NextMonth : StatisticsEvent()
}
