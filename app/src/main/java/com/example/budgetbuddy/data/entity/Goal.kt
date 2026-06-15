package com.example.budgetbuddy.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a monthly spending goal for a user.
 */
@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val month: String,   // Format: YYYY-MM
    val minAmount: Double,
    val maxAmount: Double
)