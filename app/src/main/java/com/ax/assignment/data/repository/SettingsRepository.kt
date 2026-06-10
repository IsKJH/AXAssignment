package com.ax.assignment.data.repository

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val _startDay = MutableStateFlow(preferences.getInt(KEY_START_DAY, DEFAULT_START_DAY))
    val startDay: StateFlow<Int> = _startDay.asStateFlow()

    fun setStartDay(day: Int) {
        val normalized = day.coerceIn(MIN_START_DAY, MAX_START_DAY)
        preferences.edit().putInt(KEY_START_DAY, normalized).apply()
        _startDay.value = normalized
    }

    companion object {
        const val DEFAULT_START_DAY = 1
        const val MIN_START_DAY = 1
        const val MAX_START_DAY = 28
        private const val PREF_NAME = "budget_settings"
        private const val KEY_START_DAY = "start_day"
    }
}
