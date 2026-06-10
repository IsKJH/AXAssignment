package com.ax.assignment.data.repository

import com.ax.assignment.domain.model.Category
import com.ax.assignment.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAll(): Flow<List<Category>>
    fun getByType(type: TransactionType): Flow<List<Category>>
    suspend fun getById(id: Long): Category?
    suspend fun insert(category: Category)
    suspend fun update(category: Category)
    suspend fun delete(category: Category)
}
