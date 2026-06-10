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
import com.ax.assignment.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class TransactionViewModel(
    private val transactionRepo: TransactionRepository,
    private val categoryRepo: CategoryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            categoryRepo.getAll().collect { cats ->
                val current = _uiState.value
                _uiState.value = current.copy(
                    categories = cats,
                    selectedCategory = current.selectedCategory
                        ?: cats.firstOrNull { it.type == current.type },
                )
            }
        }
    }

    fun loadTransaction(id: Long) {
        viewModelScope.launch {
            val tx = transactionRepo.getById(id) ?: return@launch
            _uiState.value = _uiState.value.copy(
                amount = tx.amount.toString(),
                type = tx.type,
                selectedCategory = tx.category,
                memo = tx.memo,
                date = tx.date,
            )
        }
    }

    fun onEvent(event: TransactionEvent) = when (event) {
        is TransactionEvent.SetAmount -> _uiState.value =
            _uiState.value.copy(amount = event.value.filter { it.isDigit() })
        is TransactionEvent.SetType -> {
            val cats = _uiState.value.categories
            _uiState.value = _uiState.value.copy(
                type = event.type,
                selectedCategory = cats.firstOrNull { it.type == event.type },
            )
        }
        is TransactionEvent.SetCategory -> _uiState.value =
            _uiState.value.copy(selectedCategory = event.category)
        is TransactionEvent.SetMemo -> _uiState.value =
            _uiState.value.copy(memo = event.memo)
        is TransactionEvent.SetDate -> _uiState.value =
            _uiState.value.copy(date = event.date)
        TransactionEvent.ToggleRecurring -> _uiState.value =
            _uiState.value.copy(isRecurring = !_uiState.value.isRecurring)
        TransactionEvent.Save -> save()
        is TransactionEvent.Delete -> delete(event.editId)
    }

    private fun delete(editId: Long) {
        viewModelScope.launch {
            val transaction = transactionRepo.getById(editId) ?: return@launch
            transactionRepo.delete(transaction)
            _uiState.value = _uiState.value.copy(isDeleted = true)
        }
    }

    private fun save() {
        val s = _uiState.value
        val amount = s.amount.toLongOrNull() ?: return
        viewModelScope.launch {
            val transaction = Transaction(
                id = 0L,
                amount = amount,
                type = s.type,
                category = s.selectedCategory,
                memo = s.memo,
                date = s.date,
                isRecurring = s.isRecurring,
            )
            if (s.isRecurring) {
                transactionRepo.insertRecurring(transaction)
            } else {
                transactionRepo.insert(transaction)
            }
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }

    companion object {
        fun factory(app: BudgetApplication): ViewModelProvider.Factory = viewModelFactory {
            initializer { TransactionViewModel(app.transactionRepository, app.categoryRepository) }
        }
    }
}
