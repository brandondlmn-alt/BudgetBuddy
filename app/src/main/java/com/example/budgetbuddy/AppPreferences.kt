package com.example.budgetbuddy

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context, userId: Int) {
    private val prefs: SharedPreferences = context.getSharedPreferences("budgetbuddy_user_prefs_$userId", Context.MODE_PRIVATE)

    fun getAvatarResId(): Int {
        return prefs.getInt("avatar_res_id", R.drawable.ic_default_avatar)
    }

    fun setAvatarResId(resId: Int) {
        prefs.edit().putInt("avatar_res_id", resId).apply()
    }
}