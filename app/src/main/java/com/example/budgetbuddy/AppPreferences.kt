package com.example.budgetbuddy

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context, userId: Int) {
    // Separate preference file for each user account
    private val prefs: SharedPreferences = context.getSharedPreferences("budgetbuddy_secure_prefs_$userId", Context.MODE_PRIVATE)

    fun getAvatarResId(): Int {
        // Return default icon if no selection exists
        return prefs.getInt("avatar_res_id", R.drawable.ic_default_avatar)
    }

    fun setAvatarResId(resId: Int) {
        prefs.edit().putInt("avatar_res_id", resId).apply()
    }

    fun getQuizScore(): Int {
        // -1 signifies the user hasn't finished the quiz
        return prefs.getInt("user_knowledge_score_v2", -1)
    }

    fun setQuizScore(score: Int) {
        // Finalize score writing before UI reload
        prefs.edit().putInt("user_knowledge_score_v2", score).commit()
    }
}