package com.ax.assignment.data.mapper

import com.ax.assignment.data.local.entity.TransactionEntity
import com.ax.assignment.domain.model.Category
import com.ax.assignment.domain.model.Transaction
import com.ax.assignment.domain.model.TransactionType

fun TransactionEntity.toDomain(category: Category?): Transaction = Transaction(
    id = id,
    amount = amount,
    type = TransactionType.valueOf(type),
    category = category,
    memo = memo,
    date = date,
    isRecurring = isRecurring,
    seriesId = seriesId,
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    amount = amount,
    type = type.name,
    categoryId = category?.id,
    memo = memo,
    date = date,
    isRecurring = isRecurring,
    seriesId = seriesId,
)
