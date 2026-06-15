package com.example.budgetbuddy.data.database

import android.content.Context

/**
 * Utility object to provide the database instance across the app.
 */
object DatabaseProvider {
    fun getDatabase(context: Context): AppDatabase {
        // Return the singleton database instance
        return AppDatabase.getDatabase(context)
    }
}