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
        is CategoryEvent.Add -> add(event.name, event.emoji, event.type)
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

    private fun add(name: String, emoji: String, type: TransactionType?) {
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
                    colorHex = pickNextColor(categories),
                    type = type,
                    sortOrder = nextSortOrder,
                ),
            )
        }
    }

    // Assign the first palette color not already used by any category,
    // so every custom category gets a distinct dot/donut color
    private fun pickNextColor(categories: List<Category>): String {
        val used = categories.map { it.colorHex.uppercase() }.toSet()
        return CUSTOM_CATEGORY_COLORS.firstOrNull { it.uppercase() !in used }
            ?: CUSTOM_CATEGORY_COLORS[categories.size % CUSTOM_CATEGORY_COLORS.size]
    }

    companion object {
        const val MAX_CUSTOM_CATEGORIES = 7

        // Distinct from the default category palette (식비~교육, 급여~기타수입)
        private val CUSTOM_CATEGORY_COLORS = listOf(
            "#7C4DFF", // 보라
            "#FF7043", // 딥 오렌지
            "#26A69A", // 틸
            "#EC407A", // 핑크
            "#5C6BC0", // 인디고
            "#9CCC65", // 라임
            "#8D6E63", // 브라운
            "#00ACC1", // 시안
        )

        fun factory(app: BudgetApplication): ViewModelProvider.Factory = viewModelFactory {
            initializer { CategoryViewModel(app.categoryRepository) }
        }
    }
}
