package com.example.budgetbuddy.data.dao

import androidx.room.*
import com.example.budgetbuddy.data.entity.Goal

/**
 * Access methods for managing spending goals in the database.
 */
@Dao
interface GoalDao {
    @Insert
    suspend fun insertGoal(goal: Goal)

    @Update
    suspend fun updateGoal(goal: Goal)

    // Retrieve a specific user's goal for a given month
    @Query("SELECT * FROM goals WHERE userId = :userId AND month = :month LIMIT 1")
    suspend fun getGoalForUserAndMonth(userId: Int, month: String): Goal?

    // Fetch all goals set by a specific user
    @Query("SELECT * FROM goals WHERE userId = :userId")
    suspend fun getAllGoals(userId: Int): List<Goal>
}