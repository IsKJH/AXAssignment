package com.ax.assignment.feature.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ax.assignment.BudgetApplication
import com.ax.assignment.core.util.currentPeriodStart
import com.ax.assignment.core.util.periodRange
import com.ax.assignment.data.repository.SettingsRepository
import com.ax.assignment.data.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModel(
    private val repo: TransactionRepository,
    private val settingsRepo: SettingsRepository,
) : ViewModel() {

    private val initialPeriodStart = currentPeriodStart(LocalDate.now(), settingsRepo.startDay.value)
    private val initialRange =
        periodRange(initialPeriodStart.year, initialPeriodStart.monthValue, settingsRepo.startDay.value)
    private val _selectedYear = MutableStateFlow(initialPeriodStart.year)
    private val _selectedMonth = MutableStateFlow(initialPeriodStart.monthValue)

    init {
        // A start-day change redefines every period boundary — snap back to the
        // current period so a live screen doesn't keep showing a stale month
        viewModelScope.launch {
            settingsRepo.startDay.drop(1).collect { day ->
                val now = currentPeriodStart(LocalDate.now(), day)
                _selectedYear.value = now.year
                _selectedMonth.value = now.monthValue
            }
        }
    }

    val uiState: StateFlow<StatisticsUiState> =
        combine(_selectedYear, _selectedMonth, settingsRepo.startDay) { year, month, startDay ->
            val range = periodRange(year, month, startDay)
            val prevBase = LocalDate.of(year, month, 1).minusMonths(1)
            val prevRange = periodRange(prevBase.year, prevBase.monthValue, startDay)
            Triple(range, prevRange, startDay)
        }
            .flatMapLatest { (range, prevRange, startDay) ->
                // Trend is anchored to the real current period so the 6-month history
                // (and its entry button) ignore which period the user is browsing
                val nowPeriodStart = currentPeriodStart(LocalDate.now(), startDay)
                combine(
                    repo.getIncomeByDateRange(range.start, range.end),
                    repo.getExpenseByDateRange(range.start, range.end),
                    repo.getExpenseByCategory(range.start, range.end),
                    repo.getExpenseByCategory(prevRange.start, prevRange.end),
                    repo.getRecentPeriodsExpense(nowPeriodStart, 6),
                ) { income, expense, categoryTotals, prevCategoryTotals, trend ->
                    StatisticsUiState(
                        isLoading = false,
                        selectedYear = _selectedYear.value,
                        selectedMonth = _selectedMonth.value,
                        totalIncome = income,
                        totalExpense = expense,
                        categoryTotals = categoryTotals,
                        prevCategoryTotals = prevCategoryTotals,
                        monthlyExpenseTrend = trend,
                        periodStart = range.start,
                        periodEnd = range.end,
                    )
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                // Seed with the real current period so the label/range doesn't flash
                // a default month before the flow emits
                StatisticsUiState(
                    selectedYear = initialPeriodStart.year,
                    selectedMonth = initialPeriodStart.monthValue,
                    periodStart = initialRange.start,
                    periodEnd = initialRange.end,
                ),
            )

    fun onEvent(event: StatisticsEvent) = when (event) {
        StatisticsEvent.PrevMonth -> moveToPrevMonth()
        StatisticsEvent.NextMonth -> moveToNextMonth()
    }

    private fun moveToPrevMonth() {
        val current = LocalDate.of(_selectedYear.value, _selectedMonth.value, 1).minusMonths(1)
        _selectedYear.value = current.year
        _selectedMonth.value = current.monthValue
    }

    private fun moveToNextMonth() {
        val current = LocalDate.of(_selectedYear.value, _selectedMonth.value, 1).plusMonths(1)
        _selectedYear.value = current.year
        _selectedMonth.value = current.monthValue
    }

    companion object {
        fun factory(app: BudgetApplication): ViewModelProvider.Factory = viewModelFactory {
            initializer { StatisticsViewModel(app.transactionRepository, app.settingsRepository) }
        }
    }
}
