package com.ax.assignment.feature.category

import com.ax.assignment.domain.model.Category
import com.ax.assignment.domain.model.TransactionType

data class CategoryUiState(
    val isLoading: Boolean = true,
    val categories: List<Category> = emptyList(),
    val showAddSheet: Boolean = false,
    val editingCategory: Category? = null,
    val deleteTargetId: Long? = null,
)

sealed class CategoryEvent {
    data class Delete(val category: Category) : CategoryEvent()
    data class Update(val category: Category, val name: String) : CategoryEvent()
    data class Add(
        val name: String,
        val emoji: String,
        val type: TransactionType?,
    ) : CategoryEvent()
}
