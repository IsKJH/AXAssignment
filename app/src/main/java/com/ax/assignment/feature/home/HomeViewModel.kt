package com.ax.assignment.feature.home

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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val repo: TransactionRepository,
    private val settingsRepo: SettingsRepository,
) : ViewModel() {

    private val initialPeriodStart = currentPeriodStart(LocalDate.now(), settingsRepo.startDay.value)
    private val _selectedYear = MutableStateFlow(initialPeriodStart.year)
    private val _selectedMonth = MutableStateFlow(initialPeriodStart.monthValue)

    val uiState: StateFlow<HomeUiState> =
        combine(_selectedYear, _selectedMonth, settingsRepo.startDay) { year, month, startDay ->
            val range = periodRange(year, month, startDay)
            Triple(year, month, range)
        }
            .flatMapLatest { (year, month, range) ->
                combine(
                    repo.getByDateRange(range.start, range.end),
                    repo.getIncomeByDateRange(range.start, range.end),
                    repo.getExpenseByDateRange(range.start, range.end),
                ) { transactions, income, expense ->
                    HomeUiState(
                        isLoading = false,
                        transactions = transactions,
                        totalIncome = income,
                        totalExpense = expense,
                        selectedYear = year,
                        selectedMonth = month,
                        periodStart = range.start,
                        periodEnd = range.end,
                    )
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun onEvent(event: HomeEvent) = when (event) {
        HomeEvent.PrevMonth -> moveToPrevMonth()
        HomeEvent.NextMonth -> moveToNextMonth()
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
            initializer { HomeViewModel(app.transactionRepository, app.settingsRepository) }
        }
    }
}
