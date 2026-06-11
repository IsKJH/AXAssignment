package com.ax.assignment.feature.transaction

import com.ax.assignment.domain.model.Category
import com.ax.assignment.domain.model.RecurringScope
import com.ax.assignment.domain.model.Transaction
import com.ax.assignment.domain.model.TransactionType
import java.time.LocalDateTime

data class TransactionDetailUiState(
    val isLoading: Boolean = true,
    val transaction: Transaction? = null,
    val error: String? = null,
    val isDeleted: Boolean = false,
    val isSaved: Boolean = false,
    // Editing mode state
    val isEditing: Boolean = false,
    val editAmount: String = "",
    val editType: TransactionType = TransactionType.EXPENSE,
    val editMemo: String = "",
    val editCategory: Category? = null,
    val editDate: LocalDateTime = LocalDateTime.now(),
    val editIsRecurring: Boolean = false,
    val categories: List<Category> = emptyList(),
) {
    val hasChanges: Boolean
        get() = transaction != null && (
            editAmount != transaction.amount.toString() ||
            editType != transaction.type ||
            editMemo != transaction.memo ||
            editCategory?.id != transaction.category?.id ||
            editDate != transaction.date ||
            editIsRecurring != transaction.isRecurring
        )
}

sealed class TransactionDetailEvent {
    data class Delete(val scope: RecurringScope? = null) : TransactionDetailEvent()
    object StartEditing : TransactionDetailEvent()
    object CancelEditing : TransactionDetailEvent()
    data class SaveEditing(val scope: RecurringScope? = null) : TransactionDetailEvent()
    object UnregisterRecurring : TransactionDetailEvent()
    data class SetAmount(val value: String) : TransactionDetailEvent()
    data class SetType(val type: TransactionType) : TransactionDetailEvent()
    data class SetMemo(val memo: String) : TransactionDetailEvent()
    data class SetCategory(val category: Category?) : TransactionDetailEvent()
    data class SetDate(val date: LocalDateTime) : TransactionDetailEvent()
    object ToggleRecurring : TransactionDetailEvent()
}
