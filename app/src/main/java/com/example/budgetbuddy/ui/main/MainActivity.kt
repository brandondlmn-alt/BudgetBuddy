package com.example.budgetbuddy.ui.main

import android.os.Bundle
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
import com.example.budgetbuddy.ui.main.fragments.*
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var bottomNav: BottomNavigationView
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
        bottomNav = findViewById(R.id.bottom_navigation)

        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        setupDrawerHeader()
        setupNavigation()

        if (savedInstanceState == null) {
            loadFragment(DashboardFragment.newInstance(userId), "Dashboard")
            navView.setCheckedItem(R.id.nav_dashboard)
            bottomNav.selectedItemId = R.id.nav_dashboard
        }
    }

    private fun setupDrawerHeader() {
        val headerView = navView.getHeaderView(0)
        val ivAvatar = headerView.findViewById<ImageView>(R.id.iv_avatar)
        val tvUsername = headerView.findViewById<TextView>(R.id.tv_username)

        ivAvatar.setImageResource(appPreferences.getAvatarResId())

        val db = DatabaseProvider.getDatabase(this)
        val userDao = db.userDao()
        
        lifecycleScope.launch {
            val user = userDao.getUserById(userId)
            tvUsername.text = user?.username ?: "User"
        }
    }

    private fun setupNavigation() {
        // Drawer Navigation
        navView.setNavigationItemSelectedListener { menuItem ->
            handleNavigation(menuItem.itemId)
            drawerLayout.closeDrawers()
            true
        }

        // Bottom Navigation
        bottomNav.setOnItemSelectedListener { menuItem ->
            handleNavigation(menuItem.itemId)
            true
        }
    }

    private fun handleNavigation(itemId: Int) {
        val fragment: Fragment = when (itemId) {
            R.id.nav_dashboard -> DashboardFragment.newInstance(userId)
            R.id.nav_add_expense, R.id.nav_add -> AddExpenseFragment.newInstance(userId)
            R.id.nav_view_expenses, R.id.nav_expenses -> ViewExpensesFragment.newInstance(userId)
            R.id.nav_categories -> CategoryManagerFragment.newInstance(userId)
            R.id.nav_graphs, R.id.nav_reports -> ReportsFragment.newInstance(userId)
            R.id.nav_progress -> GoalsFragment.newInstance(userId)
            R.id.nav_gamification -> GamificationFragment.newInstance(userId)
            R.id.nav_converter -> CurrencyConverterFragment.newInstance(userId)
            R.id.nav_profile, R.id.nav_more -> ProfileFragment.newInstance(userId)
            else -> DashboardFragment.newInstance(userId)
        }
        
        val tag = when (itemId) {
            R.id.nav_dashboard -> "Dashboard"
            R.id.nav_add_expense, R.id.nav_add -> "AddExpense"
            R.id.nav_view_expenses, R.id.nav_expenses -> "ViewExpenses"
            R.id.nav_gamification -> "Gamification"
            R.id.nav_converter -> "Converter"
            else -> "Other"
        }

        loadFragment(fragment, tag)
        
        // Sync selection between Drawer and BottomNav
        syncNavigationUI(itemId)
    }

    private fun syncNavigationUI(itemId: Int) {
        // Map common IDs if they differ between menus
        val drawerId = when(itemId) {
            R.id.nav_add -> R.id.nav_add_expense
            R.id.nav_expenses -> R.id.nav_view_expenses
            R.id.nav_reports -> R.id.nav_graphs
            R.id.nav_more -> R.id.nav_profile
            else -> itemId
        }
        
        val bottomId = when(itemId) {
            R.id.nav_add_expense -> R.id.nav_add
            R.id.nav_view_expenses -> R.id.nav_expenses
            R.id.nav_graphs -> R.id.nav_reports
            R.id.nav_profile -> R.id.nav_more
            else -> itemId
        }

        navView.setCheckedItem(drawerId)
        // Only update bottom nav if the item exists there
        bottomNav.menu.findItem(bottomId)?.let { it.isChecked = true }
    }

    fun loadFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, tag)
            .addToBackStack(null)
            .commit()
    }

    fun updateAvatarInDrawer(resId: Int) {
        val headerView = navView.getHeaderView(0)
        headerView.findViewById<ImageView>(R.id.iv_avatar).setImageResource(resId)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}