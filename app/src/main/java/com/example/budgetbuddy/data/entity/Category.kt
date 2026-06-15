package com.example.budgetbuddy.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Defines spending categories used to group expenses.
 */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val userId: Int,
    val colorCode: String = "#9E9E9E",
    val iconText: String = "?" // Usually the first letter of the category name
)