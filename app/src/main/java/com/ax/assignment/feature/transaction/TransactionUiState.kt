package com.ax.assignment.feature.transaction

import com.ax.assignment.domain.model.Category
import com.ax.assignment.domain.model.TransactionType
import java.time.LocalDateTime

data class TransactionUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val amount: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val selectedCategory: Category? = null,
    val memo: String = "",
    val date: LocalDateTime = LocalDateTime.now(),
    val isRecurring: Boolean = false,
    val categories: List<Category> = emptyList(),
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
)

sealed class TransactionEvent {
    data class SetAmount(val value: String) : TransactionEvent()
    data class SetType(val type: TransactionType) : TransactionEvent()
    data class SetCategory(val category: Category) : TransactionEvent()
    data class SetMemo(val memo: String) : TransactionEvent()
    data class SetDate(val date: LocalDateTime) : TransactionEvent()
    object ToggleRecurring : TransactionEvent()
    object Save : TransactionEvent()
    data class Delete(val editId: Long) : TransactionEvent()
}
