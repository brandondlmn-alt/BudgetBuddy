package com.example.budgetbuddy.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.budgetbuddy.AppPreferences
import com.example.budgetbuddy.R
import com.example.budgetbuddy.data.database.DatabaseProvider
import com.example.budgetbuddy.ui.auth.LoginActivity
import com.example.budgetbuddy.ui.main.fragments.*
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var appPreferences: AppPreferences
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userId = intent.getIntExtra("USER_ID", -1)
        if (userId == -1) {
            finish()
            return
        }

        appPreferences = AppPreferences(this, userId)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        setupDrawerHeader()
        setupNavigation()

        if (savedInstanceState == null) {
            loadFragment(DashboardFragment.newInstance(userId), "Dashboard")
            navView.setCheckedItem(R.id.nav_dashboard)
        }
    }

    // Public method for fragments to trigger a header refresh
    fun refreshHeader() {
        runOnUiThread {
            setupDrawerHeader()
        }
    }

    private fun setupDrawerHeader() {
        val headerView = navView.getHeaderView(0) ?: return

        val ivAvatar = headerView.findViewById<ImageView>(R.id.iv_avatar)
        val tvUsername = headerView.findViewById<TextView>(R.id.tv_username)
        val ivBadge = headerView.findViewById<ImageView>(R.id.iv_header_badge)
        val tvTier = headerView.findViewById<TextView>(R.id.tv_user_tier)

        // Load Avatar from Preferences
        ivAvatar?.setImageResource(appPreferences.getAvatarResId())

        // Load all User-specific data from the Database to ensure fresh data per account
        val db = DatabaseProvider.getDatabase(this)
        lifecycleScope.launch {
            val user = db.userDao().getUserById(userId)

            // 1. Update Username
            tvUsername?.text = user?.username ?: "User"

            // 2. Update Badge and Tier logic from Database score
            val score = user?.quizScore ?: -1
            if (score >= 0) {
                ivBadge?.visibility = View.VISIBLE
                when {
                    score >= 90 -> {
                        ivBadge?.setImageResource(R.drawable.diamond_badge)
                        tvTier?.text = "Financial Guru"
                    }
                    score >= 70 -> {
                        ivBadge?.setImageResource(R.drawable.gold_medal)
                        tvTier?.text = "Budget Expert"
                    }
                    score >= 40 -> {
                        ivBadge?.setImageResource(R.drawable.silver_badge)
                        tvTier?.text = "Money Smart"
                    }
                    else -> {
                        ivBadge?.setImageResource(R.drawable.bronze_badge)
                        tvTier?.text = "Financial Novice"
                    }
                }
            } else {
                // Default state for new accounts
                ivBadge?.visibility = View.GONE
                tvTier?.text = "New Member"
            }
        }
    }

    private fun setupNavigation() {
        navView.setNavigationItemSelectedListener { menuItem ->
            if (menuItem.itemId == R.id.nav_logout) {
                logout()
            } else {
                handleNavigation(menuItem.itemId)
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun handleNavigation(itemId: Int) {
        val fragment: Fragment = when (itemId) {
            R.id.nav_dashboard -> DashboardFragment.newInstance(userId)
            R.id.nav_add_expense -> AddExpenseFragment.newInstance(userId)
            R.id.nav_view_expenses -> ViewExpensesFragment.newInstance(userId)
            R.id.nav_categories -> CategoryManagerFragment.newInstance(userId)
            R.id.nav_graphs -> ReportsFragment.newInstance(userId)
            R.id.nav_progress -> GoalsFragment.newInstance(userId)
            R.id.nav_gamification -> GamificationFragment.newInstance(userId)
            R.id.nav_converter -> CurrencyConverterFragment.newInstance(userId)
            R.id.nav_profile -> ProfileFragment.newInstance(userId)
            else -> DashboardFragment.newInstance(userId)
        }
        
        val tag = when (itemId) {
            R.id.nav_dashboard -> "Dashboard"
            R.id.nav_add_expense -> "AddExpense"
            R.id.nav_view_expenses -> "ViewExpenses"
            R.id.nav_gamification -> "Gamification"
            R.id.nav_converter -> "Converter"
            else -> "Other"
        }

        loadFragment(fragment, tag)
        navView.setCheckedItem(itemId)
    }

    private fun logout() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    fun loadFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, tag)
            .addToBackStack(null)
            .commit()
    }

    fun updateAvatarInDrawer(resId: Int) {
        setupDrawerHeader()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}