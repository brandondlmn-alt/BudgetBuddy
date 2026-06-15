package com.example.budgetbuddy

import android.app.Application
import com.example.budgetbuddy.data.database.DatabaseProvider

/**
 * Main application class used to initialize global state and the database.
 */
class BudgetBuddyApplication : Application() {
    // Lazily initialize the database when it's first needed
    val database by lazy { DatabaseProvider.getDatabase(this) }
}