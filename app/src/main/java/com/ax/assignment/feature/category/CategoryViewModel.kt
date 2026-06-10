package com.ax.assignment.feature.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ax.assignment.BudgetApplication
import com.ax.assignment.data.repository.CategoryRepository
import com.ax.assignment.domain.model.Category
import com.ax.assignment.domain.model.TransactionType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoryViewModel(private val repo: CategoryRepository) : ViewModel() {

    val uiState: StateFlow<CategoryUiState> = repo.getAll()
        .map { CategoryUiState(isLoading = false, categories = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CategoryUiState())

    fun onEvent(event: CategoryEvent) = when (event) {
        is CategoryEvent.Delete -> delete(event.category)
        is CategoryEvent.Update -> update(event.category, event.name)
        is CategoryEvent.Add -> add(event.name, event.emoji, event.colorHex, event.type)
    }

    private fun delete(category: Category) {
        if (!category.isDefault) {
            viewModelScope.launch { repo.delete(category) }
        }
    }

    private fun update(category: Category, name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank() || category.isDefault) return
        viewModelScope.launch {
            repo.update(category.copy(name = trimmed))
        }
    }

    private fun add(name: String, emoji: String, colorHex: String, type: TransactionType?) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        val categories = uiState.value.categories
        val categoriesOfType = categories.filter { it.type == type }
        val customCount = categoriesOfType.count { !it.isDefault }
        if (customCount >= MAX_CUSTOM_CATEGORIES) return
        val nextSortOrder = (categoriesOfType.maxOfOrNull { it.sortOrder } ?: 0) + 1
        viewModelScope.launch {
            repo.insert(
                Category(
                    name = trimmed,
                    emoji = emoji,
                    colorHex = colorHex,
                    type = type,
                    sortOrder = nextSortOrder,
                ),
            )
        }
    }

    companion object {
        const val MAX_CUSTOM_CATEGORIES = 7

        fun factory(app: BudgetApplication): ViewModelProvider.Factory = viewModelFactory {
            initializer { CategoryViewModel(app.categoryRepository) }
        }
    }
}
