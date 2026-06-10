package com.ax.assignment

import android.app.Application
import com.ax.assignment.data.local.AppDatabase
import com.ax.assignment.data.repository.CategoryRepository
import com.ax.assignment.data.repository.CategoryRepositoryImpl
import com.ax.assignment.data.repository.SettingsRepository
import com.ax.assignment.data.repository.TransactionRepository
import com.ax.assignment.data.repository.TransactionRepositoryImpl

class BudgetApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(this)
    }
    val transactionRepository: TransactionRepository by lazy {
        TransactionRepositoryImpl(database.transactionDao(), database.categoryDao())
    }
    val categoryRepository: CategoryRepository by lazy {
        CategoryRepositoryImpl(database.categoryDao())
    }
}
