package com.ax.assignment.data.repository

import com.ax.assignment.domain.model.CategorySummary
import com.ax.assignment.domain.model.Transaction
import com.ax.assignment.domain.model.MonthlyExpense
import com.ax.assignment.domain.model.RecurringScope
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TransactionRepository {
    fun getByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>>
    fun getIncomeByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<Long>
    fun getExpenseByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<Long>
    fun getExpenseByCategory(startDate: LocalDate, endDate: LocalDate): Flow<List<CategorySummary>>
    fun getRecentPeriodsExpense(startDate: LocalDate, count: Int): Flow<List<MonthlyExpense>>
    suspend fun insert(transaction: Transaction): Long
    suspend fun update(transaction: Transaction)
    suspend fun delete(transaction: Transaction)
    suspend fun getById(id: Long): Transaction?
    suspend fun insertRecurring(transaction: Transaction): Long
    suspend fun updateRecurring(transaction: Transaction, scope: RecurringScope)
    suspend fun deleteRecurring(transaction: Transaction, scope: RecurringScope)
    suspend fun unregisterRecurring(transaction: Transaction)
    suspend fun registerRecurring(transaction: Transaction)
}
