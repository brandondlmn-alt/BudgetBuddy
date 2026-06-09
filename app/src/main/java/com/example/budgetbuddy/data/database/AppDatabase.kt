package com.example.budgetbuddy.data.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.budgetbuddy.data.entity.*
import com.example.budgetbuddy.data.dao.*

@Database(
    entities = [User::class, Category::class, Expense::class, Goal::class],
    version = 32, // Incremented to 32 for quizScore inclusion in User entity
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun goalDao(): GoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budgetbuddy_db"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        try {
                            db.execSQL("INSERT OR IGNORE INTO categories (id, name, userId, colorCode, iconText) VALUES (1, 'Food', 0, '#4CAF50', 'F')")
                            db.execSQL("INSERT OR IGNORE INTO categories (id, name, userId, colorCode, iconText) VALUES (2, 'Transport', 0, '#2196F3', 'T')")
                            db.execSQL("INSERT OR IGNORE INTO categories (id, name, userId, colorCode, iconText) VALUES (3, 'Rent', 0, '#F44336', 'R')")
                            db.execSQL("INSERT OR IGNORE INTO categories (id, name, userId, colorCode, iconText) VALUES (4, 'Entertainment', 0, '#9C27B0', 'E')")
                            db.execSQL("INSERT OR IGNORE INTO categories (id, name, userId, colorCode, iconText) VALUES (5, 'Health', 0, '#FF9800', 'H')")
                        } catch (e: Exception) {
                            Log.e("AppDatabase", "Seeding failed safely")
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}