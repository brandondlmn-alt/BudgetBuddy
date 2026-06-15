package com.example.budgetbuddy.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetbuddy.BudgetBuddyApplication
import com.example.budgetbuddy.data.entity.Category
import com.example.budgetbuddy.data.entity.User
import kotlinx.coroutines.launch
import java.security.MessageDigest

/**
 * Handles the logic for user authentication, including login, registration,
 * and initial account setup.
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as BudgetBuddyApplication).database

    // Verifies user credentials and sets up default data if login is successful
    fun login(username: String, password: String, onResult: (Int?) -> Unit) {
        viewModelScope.launch {
            val user = db.userDao().getUserByUsername(username)
            if (user != null && user.passwordHash == hashPassword(password)) {
                insertDefaultCategories(user.id)
                onResult(user.id)
            } else {
                onResult(null)
            }
        }
    }

    // Creates a new user account if the username isn't already taken
    fun register(username: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val existing = db.userDao().getUserByUsername(username)
            if (existing != null) {
                onResult(false)
            } else {
                val newUser = User(username = username, passwordHash = hashPassword(password))
                val newUserId = db.userDao().insertUser(newUser).toInt()
                insertDefaultCategories(newUserId)
                onResult(true)
            }
        }
    }

    // Adds a standard set of categories for new users to start with
    private fun insertDefaultCategories(userId: Int) {
        viewModelScope.launch {
            val existing = db.categoryDao().getCategoriesByUser(userId)
            if (existing.isEmpty()) {
                val defaults = listOf(
                    Category(name = "Transport", userId = userId, colorCode = "#FF9800", iconText = "T"),
                    Category(name = "Rent", userId = userId, colorCode = "#E53935", iconText = "R"),
                    Category(name = "Groceries", userId = userId, colorCode = "#4CAF50", iconText = "G"),
                    Category(name = "Entertainment", userId = userId, colorCode = "#9C27B0", iconText = "E"),
                    Category(name = "Dining", userId = userId, colorCode = "#FFC107", iconText = "D")
                )
                for (cat in defaults) {
                    db.categoryDao().insertCategory(cat)
                }
            }
        }
    }

    // Standard SHA-256 hashing for storing passwords securely
    private fun hashPassword(password: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }
}