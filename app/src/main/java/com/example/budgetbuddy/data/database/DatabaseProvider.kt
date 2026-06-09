package com.example.budgetbuddy.data.database

import android.content.Context

object DatabaseProvider {
    fun getDatabase(context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }
}