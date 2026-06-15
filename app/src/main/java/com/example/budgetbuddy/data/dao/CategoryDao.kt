package com.example.budgetbuddy.data.dao

import androidx.room.*
import com.example.budgetbuddy.data.entity.Category

/**
 * Interface for database interactions related to expense categories.
 */
@Dao
interface CategoryDao {
    @Insert
    suspend fun insertCategory(category: Category)

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    // Gets all categories created by or assigned to a specific user
    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name ASC")
    suspend fun getCategoriesByUser(userId: Int): List<Category>

    // Fetches a single category by its ID
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): Category?
}