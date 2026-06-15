package com.example.budgetbuddy.data.dao

import androidx.room.*
import com.example.budgetbuddy.data.entity.User

/**
 * Handles database operations for user accounts.
 */
@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    // Lookup user by their unique username (used during login)
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    // Fetch user details using their unique database ID
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): User?
}