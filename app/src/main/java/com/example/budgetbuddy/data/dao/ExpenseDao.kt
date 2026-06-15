package com.example.budgetbuddy.data.dao

import androidx.room.*
import com.example.budgetbuddy.data.entity.Expense

/**
 * Data access object for handling all expense-related queries.
 */
@Dao
interface ExpenseDao {
    @Insert
    suspend fun insertExpense(expense: Expense)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    // Pulls expenses for a specific user within a date range, newest first
    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC, startTime DESC")
    suspend fun getExpensesByDateRange(userId: Int, startDate: String, endDate: String): List<Expense>

    // Calculates total spent in a specific category for a date range
    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND categoryId = :categoryId AND date BETWEEN :startDate AND :endDate")
    suspend fun getCategoryTotal(userId: Int, categoryId: Int, startDate: String, endDate: String): Double?

    // Calculates total overall spending for a user in a given period
    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalSpent(userId: Int, startDate: String, endDate: String): Double?

    // Counts all transactions logged by a user
    @Query("SELECT COUNT(*) FROM expenses WHERE userId = :userId")
    suspend fun getTotalExpenseCount(userId: Int): Int

    // Counts transactions that have an attached receipt image
    @Query("SELECT COUNT(*) FROM expenses WHERE userId = :userId AND photoPath IS NOT NULL")
    suspend fun getReceiptCount(userId: Int): Int
}