package com.ax.assignment.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ax.assignment.data.local.dao.CategoryDao
import com.ax.assignment.data.local.dao.TransactionDao
import com.ax.assignment.data.local.entity.CategoryEntity
import com.ax.assignment.data.local.entity.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TransactionEntity::class, CategoryEntity::class],
    version = 5,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "budget_db")
                    // Fresh installs start from the bundled demo DB (114 transactions)
                    // so reviewers can explore without typing data; existing installs
                    // keep their own DB untouched
                    .createFromAsset("budget_db.db")
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { db ->
                        INSTANCE = db
                        // Seed categories if empty (handles both fresh install and destructive migration)
                        CoroutineScope(Dispatchers.IO).launch {
                            if (db.categoryDao().count() == 0) {
                                seedDefaultCategories(db.categoryDao())
                            }
                        }
                    }
            }
        }

        private suspend fun seedDefaultCategories(dao: CategoryDao) {
            val defaults = listOf(
                CategoryEntity(name = "식비", emoji = "🍽️", colorHex = "#FFAC11", type = "EXPENSE", isDefault = true, sortOrder = 0),
                CategoryEntity(name = "교통", emoji = "🚌", colorHex = "#1872FA", type = "EXPENSE", isDefault = true, sortOrder = 1),
                CategoryEntity(name = "쇼핑", emoji = "🛍️", colorHex = "#BA68C8", type = "EXPENSE", isDefault = true, sortOrder = 2),
                CategoryEntity(name = "의료", emoji = "💊", colorHex = "#F06292", type = "EXPENSE", isDefault = true, sortOrder = 3),
                CategoryEntity(name = "여가", emoji = "🎬", colorHex = "#4DD0E1", type = "EXPENSE", isDefault = true, sortOrder = 4),
                CategoryEntity(name = "주거", emoji = "🏠", colorHex = "#81C784", type = "EXPENSE", isDefault = true, sortOrder = 5),
                CategoryEntity(name = "교육", emoji = "📚", colorHex = "#90A4AE", type = "EXPENSE", isDefault = true, sortOrder = 6),
            )
            dao.insertAll(defaults)
        }
    }
}
