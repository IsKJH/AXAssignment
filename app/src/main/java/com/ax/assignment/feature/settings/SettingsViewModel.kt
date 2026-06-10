package com.ax.assignment.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ax.assignment.BudgetApplication
import com.ax.assignment.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(private val repo: SettingsRepository) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = repo.startDay
        .map { SettingsUiState(startDay = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState(startDay = repo.startDay.value),
        )

    fun onEvent(event: SettingsEvent) = when (event) {
        is SettingsEvent.SetStartDay -> repo.setStartDay(event.day)
    }

    companion object {
        fun factory(app: BudgetApplication): ViewModelProvider.Factory = viewModelFactory {
            initializer { SettingsViewModel(app.settingsRepository) }
        }
    }
}
