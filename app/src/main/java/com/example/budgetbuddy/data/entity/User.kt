package com.example.budgetbuddy.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * User account details and profile information.
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val passwordHash: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val age: Int? = null,
    val quizScore: Int = -1 // -1 means the financial quiz hasn't been completed
)