package com.ax.assignment.feature.settings

data class SettingsUiState(
    val startDay: Int = 1,
)

sealed class SettingsEvent {
    data class SetStartDay(val day: Int) : SettingsEvent()
}
