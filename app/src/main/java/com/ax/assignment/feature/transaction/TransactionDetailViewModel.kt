package com.ax.assignment.feature.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ax.assignment.BudgetApplication
import com.ax.assignment.data.repository.CategoryRepository
import com.ax.assignment.data.repository.TransactionRepository
import com.ax.assignment.domain.model.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TransactionDetailViewModel(
    private val repo: TransactionRepository,
    private val categoryRepo: CategoryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionDetailUiState())
    val uiState: StateFlow<TransactionDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            categoryRepo.getAll().collect { cats ->
                _uiState.value = _uiState.value.copy(categories = cats)
            }
        }
    }

    fun loadTransaction(id: Long) {
        // Re-entering composition (e.g. returning from category select) re-fires the
        // calling LaunchedEffect; skip reloading so in-progress edits are not overwritten
        if (_uiState.value.transaction?.id == id) return
        viewModelScope.launch {
            val transaction = repo.getById(id)
            _uiState.value = if (transaction == null) {
                _uiState.value.copy(isLoading = false, error = "거래 내역을 찾을 수 없어요")
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    transaction = transaction,
                    editAmount = transaction.amount.toString(),
                    editType = transaction.type,
                    editMemo = transaction.memo,
                    editCategory = transaction.category,
                    editDate = transaction.date,
                    editIsRecurring = transaction.isRecurring,
                )
            }
        }
    }

    fun onEvent(event: TransactionDetailEvent) = when (event) {
        is TransactionDetailEvent.Delete -> delete(event.scope)
        TransactionDetailEvent.StartEditing -> _uiState.value = _uiState.value.copy(isEditing = true)
        TransactionDetailEvent.CancelEditing -> {
            val tx = _uiState.value.transaction
            if (tx != null) {
                _uiState.value = _uiState.value.copy(
                    isEditing = false,
                    editAmount = tx.amount.toString(),
                    editType = tx.type,
                    editMemo = tx.memo,
                    editCategory = tx.category,
                    editDate = tx.date,
                    editIsRecurring = false,
                )
            } else {
                _uiState.value = _uiState.value.copy(isEditing = false)
            }
        }
        is TransactionDetailEvent.SaveEditing -> save(event.scope)
        TransactionDetailEvent.UnregisterRecurring -> unregisterRecurring()
        is TransactionDetailEvent.SetAmount ->
            _uiState.value = _uiState.value.copy(editAmount = event.value.filter { it.isDigit() })
        is TransactionDetailEvent.SetType ->
            _uiState.value = _uiState.value.copy(editType = event.type, editCategory = null)
        is TransactionDetailEvent.SetMemo ->
            _uiState.value = _uiState.value.copy(editMemo = event.memo)
        is TransactionDetailEvent.SetCategory ->
            _uiState.value = _uiState.value.copy(editCategory = event.category)
        is TransactionDetailEvent.SetDate ->
            _uiState.value = _uiState.value.copy(editDate = event.date)
        TransactionDetailEvent.ToggleRecurring ->
            _uiState.value = _uiState.value.copy(editIsRecurring = !_uiState.value.editIsRecurring)
    }

    // 502:1277 — apply edits, drop future series instances, keep this one as non-recurring
    private fun unregisterRecurring() {
        val s = _uiState.value
        val tx = s.transaction ?: return
        val amount = s.editAmount.toLongOrNull() ?: return
        viewModelScope.launch {
            val updated = Transaction(
                id = tx.id,
                amount = amount,
                type = s.editType,
                category = s.editCategory,
                memo = s.editMemo,
                date = s.editDate,
                isRecurring = false,
                seriesId = tx.seriesId,
            )
            repo.unregisterRecurring(updated)
            _uiState.value = _uiState.value.copy(
                isSaved = true,
                transaction = updated.copy(seriesId = null),
                isEditing = false,
            )
        }
    }

    private fun delete(scope: RecurringScope?) {
        val transaction = _uiState.value.transaction ?: return
        viewModelScope.launch {
            if (scope != null && transaction.seriesId != null) {
                repo.deleteRecurring(transaction, scope)
            } else {
                repo.delete(transaction)
            }
            _uiState.value = _uiState.value.copy(isDeleted = true)
        }
    }

    private fun save(scope: RecurringScope?) {
        val s = _uiState.value
        val tx = s.transaction ?: return
        val amount = s.editAmount.toLongOrNull() ?: return
        viewModelScope.launch {
            val updated = Transaction(
                id = tx.id,
                amount = amount,
                type = s.editType,
                category = s.editCategory,
                memo = s.editMemo,
                date = s.editDate,
                isRecurring = s.editIsRecurring,
                seriesId = tx.seriesId,
            )
            if (scope != null && tx.seriesId != null) {
                repo.updateRecurring(updated, scope)
            } else {
                repo.update(updated)
            }
            _uiState.value = _uiState.value.copy(
                isSaved = true,
                transaction = updated,
                isEditing = false,
            )
        }
    }

    companion object {
        fun factory(app: BudgetApplication): ViewModelProvider.Factory = viewModelFactory {
            initializer { TransactionDetailViewModel(app.transactionRepository, app.categoryRepository) }
        }
    }
}
