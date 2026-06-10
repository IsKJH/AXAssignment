package com.ax.assignment.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ax.assignment.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date >= :startMs AND date <= :endMs ORDER BY date DESC")
    fun getByDateRange(startMs: Long, endMs: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME' AND date >= :startMs AND date <= :endMs")
    fun getTotalIncome(startMs: Long, endMs: Long): Flow<Long?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND date >= :startMs AND date <= :endMs")
    fun getTotalExpense(startMs: Long, endMs: Long): Flow<Long?>

    @Query("SELECT categoryId, SUM(amount) as total FROM transactions WHERE type = 'EXPENSE' AND date >= :startMs AND date <= :endMs GROUP BY categoryId")
    fun getExpenseByCategory(startMs: Long, endMs: Long): Flow<List<CategoryTotal>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND date >= :startMs AND date <= :endMs")
    fun getTotalExpenseInRange(startMs: Long, endMs: Long): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("UPDATE transactions SET seriesId = :seriesId WHERE id = :id")
    suspend fun setSeriesId(id: Long, seriesId: Long)

    @Query(
        "UPDATE transactions SET amount = :amount, type = :type, categoryId = :categoryId, memo = :memo " +
            "WHERE seriesId = :seriesId AND date >= :fromMs",
    )
    suspend fun updateSeriesFrom(seriesId: Long, fromMs: Long, amount: Long, type: String, categoryId: Long?, memo: String)

    @Query(
        "UPDATE transactions SET amount = :amount, type = :type, categoryId = :categoryId, memo = :memo " +
            "WHERE seriesId = :seriesId",
    )
    suspend fun updateSeriesAll(seriesId: Long, amount: Long, type: String, categoryId: Long?, memo: String)

    @Query("DELETE FROM transactions WHERE seriesId = :seriesId AND date >= :fromMs")
    suspend fun deleteSeriesFrom(seriesId: Long, fromMs: Long)

    @Query("DELETE FROM transactions WHERE seriesId = :seriesId")
    suspend fun deleteSeriesAll(seriesId: Long)
}

data class CategoryTotal(
    val categoryId: Long?,
    val total: Long,
)
