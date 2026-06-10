package com.ax.assignment.data.repository

import com.ax.assignment.data.local.dao.CategoryDao
import com.ax.assignment.data.local.dao.TransactionDao
import com.ax.assignment.data.mapper.toDomain
import com.ax.assignment.data.mapper.toEntity
import com.ax.assignment.domain.model.CategorySummary
import com.ax.assignment.domain.model.Transaction
import com.ax.assignment.domain.model.TransactionType
import com.ax.assignment.feature.statistics.MonthlyExpense
import com.ax.assignment.feature.transaction.RecurringScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
) : TransactionRepository {

    override fun getByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>> {
        val (start, end) = dateRange(startDate, endDate)
        return getByDateRangeMillis(start, end)
    }

    private fun getByDateRangeMillis(start: Long, end: Long): Flow<List<Transaction>> {
        return transactionDao.getByDateRange(start, end)
            .combine(categoryDao.getAll()) { txEntities, catEntities ->
                val catMap = catEntities.associateBy { it.id }
                txEntities.map { tx ->
                    tx.toDomain(tx.categoryId?.let { catMap[it]?.toDomain() })
                }
            }
    }

    override fun getIncomeByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<Long> {
        val (start, end) = dateRange(startDate, endDate)
        return transactionDao.getTotalIncome(start, end).map { it ?: 0L }
    }

    override fun getExpenseByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<Long> {
        val (start, end) = dateRange(startDate, endDate)
        return transactionDao.getTotalExpense(start, end).map { it ?: 0L }
    }

    override fun getExpenseByCategory(startDate: LocalDate, endDate: LocalDate): Flow<List<CategorySummary>> {
        val (start, end) = dateRange(startDate, endDate)
        return getExpenseByCategoryMillis(start, end)
    }

    private fun getExpenseByCategoryMillis(start: Long, end: Long): Flow<List<CategorySummary>> {
        return transactionDao.getExpenseByCategory(start, end)
            .combine(categoryDao.getAll()) { totals, catEntities ->
                val totalMap = totals.associateBy { it.categoryId }
                val expenseCategories = catEntities
                    .map { it.toDomain() }
                    .filter { it.type == TransactionType.EXPENSE }
                    .sortedWith(compareBy({ it.sortOrder }, { it.id }))

                val categorizedTotals = expenseCategories.map { category ->
                    CategorySummary(
                        category = category,
                        totalAmount = totalMap[category.id]?.total ?: 0L,
                    )
                }
                val uncategorizedTotal = totalMap[null]?.total ?: 0L
                if (uncategorizedTotal > 0L) {
                    categorizedTotals + CategorySummary(category = null, totalAmount = uncategorizedTotal)
                } else {
                    categorizedTotals
                }
            }
    }

    override fun getRecentPeriodsExpense(startDate: LocalDate, count: Int): Flow<List<MonthlyExpense>> {
        if (count <= 0) return flowOf(emptyList())
        val periods = (0 until count).map { offset ->
            val start = startDate.minusMonths((count - 1 - offset).toLong())
            Triple(start.year, start.monthValue, getExpenseByDateRange(start, start.plusMonths(1).minusDays(1)))
        }
        return combine(periods.map { it.third }) { totals ->
            periods.mapIndexed { idx, (y, m, _) ->
                MonthlyExpense(year = y, month = m, total = totals[idx])
            }
        }
    }

    override suspend fun insert(transaction: Transaction): Long =
        transactionDao.insert(transaction.toEntity())

    override suspend fun update(transaction: Transaction) =
        transactionDao.update(transaction.toEntity())

    override suspend fun delete(transaction: Transaction) =
        transactionDao.delete(transaction.toEntity())

    override suspend fun getById(id: Long): Transaction? {
        val txEntity = transactionDao.getById(id) ?: return null
        val catEntity = txEntity.categoryId?.let { categoryDao.getById(it) }
        return txEntity.toDomain(catEntity?.toDomain())
    }

    override suspend fun insertRecurring(transaction: Transaction): Long {
        val anchorId = transactionDao.insert(transaction.toEntity())
        transactionDao.setSeriesId(anchorId, anchorId)
        val futureInstances = (1L until RECURRING_MONTHS).map { offset ->
            transaction.toEntity().copy(
                id = 0L,
                date = transaction.date.plusMonths(offset),
                seriesId = anchorId,
            )
        }
        transactionDao.insertAll(futureInstances)
        return anchorId
    }

    override suspend fun updateRecurring(transaction: Transaction, scope: RecurringScope) {
        val seriesId = transaction.seriesId
        if (seriesId == null) {
            transactionDao.update(transaction.toEntity())
            return
        }
        when (scope) {
            RecurringScope.THIS_MONTH -> transactionDao.update(transaction.toEntity())
            RecurringScope.THIS_AND_FUTURE -> transactionDao.updateSeriesFrom(
                seriesId = seriesId,
                fromMs = transaction.date.toEpochMillis(),
                amount = transaction.amount,
                type = transaction.type.name,
                categoryId = transaction.category?.id,
                memo = transaction.memo,
            )
            RecurringScope.ALL -> transactionDao.updateSeriesAll(
                seriesId = seriesId,
                amount = transaction.amount,
                type = transaction.type.name,
                categoryId = transaction.category?.id,
                memo = transaction.memo,
            )
        }
    }

    override suspend fun deleteRecurring(transaction: Transaction, scope: RecurringScope) {
        val seriesId = transaction.seriesId
        if (seriesId == null) {
            transactionDao.delete(transaction.toEntity())
            return
        }
        when (scope) {
            RecurringScope.THIS_MONTH -> transactionDao.delete(transaction.toEntity())
            RecurringScope.THIS_AND_FUTURE ->
                transactionDao.deleteSeriesFrom(seriesId, transaction.date.toEpochMillis())
            RecurringScope.ALL -> transactionDao.deleteSeriesAll(seriesId)
        }
    }

    override suspend fun unregisterRecurring(transaction: Transaction) {
        val seriesId = transaction.seriesId
        if (seriesId != null) {
            // Remove only future instances; keep this one as a normal transaction
            transactionDao.deleteSeriesFrom(seriesId, transaction.date.toEpochMillis() + 1)
        }
        transactionDao.update(
            transaction.copy(isRecurring = false, seriesId = null).toEntity(),
        )
    }

    private fun LocalDateTime.toEpochMillis(): Long =
        atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun dateRange(startDate: LocalDate, endDate: LocalDate): Pair<Long, Long> {
        val start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
        return start to end
    }

    companion object {
        // Number of monthly instances created per recurring registration (anchor + 11 future)
        const val RECURRING_MONTHS = 12L
    }
}
