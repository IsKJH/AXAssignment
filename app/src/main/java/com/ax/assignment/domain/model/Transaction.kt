package com.ax.assignment.domain.model

import java.time.LocalDateTime

data class Transaction(
    val id: Long = 0,
    val amount: Long,
    val type: TransactionType,
    val category: Category?,
    val memo: String = "",
    val date: LocalDateTime,
    val isRecurring: Boolean = false,
    val seriesId: Long? = null,
)
