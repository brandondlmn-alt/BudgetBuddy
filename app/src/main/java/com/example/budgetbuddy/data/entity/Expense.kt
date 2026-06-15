package com.example.budgetbuddy.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Data model representing an individual financial transaction.
 */
@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val categoryId: Int,
    val amount: Double,
    val date: String,           // Format: YYYY-MM-DD
    val startTime: String,      // Format: HH:mm
    val endTime: String,        // Format: HH:mm
    val description: String,
    val photoPath: String? = null, // Storage path for the receipt image
    val originalCurrency: String? = null,
    val homeCurrencyAmount: Double
)