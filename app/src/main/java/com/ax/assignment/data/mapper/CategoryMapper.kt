package com.ax.assignment.data.mapper

import com.ax.assignment.data.local.entity.CategoryEntity
import com.ax.assignment.domain.model.Category
import com.ax.assignment.domain.model.TransactionType

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    emoji = emoji,
    colorHex = colorHex,
    type = when (type) {
        "INCOME" -> TransactionType.INCOME
        "EXPENSE" -> TransactionType.EXPENSE
        else -> null
    },
    isDefault = isDefault,
    sortOrder = sortOrder,
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    emoji = emoji,
    colorHex = colorHex,
    type = type?.name ?: "ALL",
    isDefault = isDefault,
    sortOrder = sortOrder,
)
