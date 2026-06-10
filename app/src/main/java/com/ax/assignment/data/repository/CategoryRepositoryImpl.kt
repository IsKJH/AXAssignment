package com.ax.assignment.data.repository

import com.ax.assignment.data.local.dao.CategoryDao
import com.ax.assignment.data.mapper.toDomain
import com.ax.assignment.data.mapper.toEntity
import com.ax.assignment.domain.model.Category
import com.ax.assignment.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(
    private val dao: CategoryDao,
) : CategoryRepository {

    override fun getAll(): Flow<List<Category>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getByType(type: TransactionType): Flow<List<Category>> =
        dao.getByType(type.name).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Category? =
        dao.getById(id)?.toDomain()

    override suspend fun insert(category: Category) {
        dao.insert(category.toEntity())
    }

    override suspend fun update(category: Category) {
        dao.update(category.toEntity())
    }

    override suspend fun delete(category: Category) {
        dao.delete(category.toEntity())
    }
}
