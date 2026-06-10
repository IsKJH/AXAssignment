package com.ax.assignment.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL,
        )
    ],
    indices = [Index("categoryId")],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Long,
    val type: String,
    val categoryId: Long?,
    val memo: String = "",
    val date: LocalDateTime,
    val isRecurring: Boolean = false,
    val seriesId: Long? = null,
)
