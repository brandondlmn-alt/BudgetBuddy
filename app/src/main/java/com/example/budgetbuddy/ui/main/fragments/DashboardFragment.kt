package com.example.budgetbuddy.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.budgetbuddy.R
import com.example.budgetbuddy.ui.CircularProgressView
import com.example.budgetbuddy.ui.main.viewmodels.DashboardViewModel
import com.example.budgetbuddy.ui.main.viewmodels.DashboardViewModelFactory
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private lateinit var viewModel: DashboardViewModel
    private lateinit var progressSpent: CircularProgressView
    private lateinit var progressRemaining: CircularProgressView
    private lateinit var tvSpent: TextView
    private lateinit var tvRemaining: TextView
    private lateinit var tvStats: TextView
    private lateinit var containerCategories: LinearLayout
    private var userId: Int = -1

    companion object {
        fun newInstance(userId: Int) = DashboardFragment().apply {
            arguments = Bundle().apply { putInt("USER_ID", userId) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        userId = arguments?.getInt("USER_ID") ?: -1

        progressSpent = view.findViewById(R.id.progress_spent)
        progressRemaining = view.findViewById(R.id.progress_remaining)
        tvSpent = view.findViewById(R.id.tv_spent)
        tvRemaining = view.findViewById(R.id.tv_remaining)
        tvStats = view.findViewById(R.id.tv_stats)
        containerCategories = view.findViewById(R.id.container_categories)

        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        view.findViewById<TextView>(R.id.tv_month_title).text = monthFormat.format(Date())

        val factory = DashboardViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, factory)[DashboardViewModel::class.java]
        viewModel.load(userId)

        viewModel.state.observe(viewLifecycleOwner) { data ->
            updateUI(data)
        }

        view.findViewById<Button>(R.id.btn_add_expense).setOnClickListener {
            val frag = AddExpenseFragment.newInstance(userId)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, frag)
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    private fun updateUI(data: com.example.budgetbuddy.ui.main.viewmodels.DashboardViewModel.DashboardData) {
        val spent = data.totalSpent
        val budget = data.maxBudget
        
        tvSpent.text = "R ${"%.2f".format(spent)}"
        
        // Setup Spent Graph (Fills up)
        progressSpent.maxProgress = budget.toFloat().let { if (it <= 0) 100f else it }
        progressSpent.progress = spent.toFloat()
        progressSpent.isReverse = false
        
        // Setup Remaining Graph (Depletes)
        progressRemaining.maxProgress = budget.toFloat().let { if (it <= 0) 100f else it }
        progressRemaining.progress = spent.toFloat()
        progressRemaining.isReverse = true
        
        val remaining = (budget - spent).coerceAtLeast(0.0)
        tvRemaining.text = "R ${"%.2f".format(remaining)}"

        if (budget > 0) {
            val percent = (spent / budget) * 100
            
            // Color logic for Spent Graph
            if (percent > 100) {
                progressSpent.setProgressColor(resources.getColor(R.color.red, null))
                progressSpent.centerText = "Over!"
            } else {
                progressSpent.setProgressColor(resources.getColor(R.color.primary, null))
                progressSpent.centerText = "${percent.toInt()}%"
            }

            // Color logic for Remaining Graph
            if (remaining <= 0) {
                progressRemaining.setProgressColor(resources.getColor(R.color.red, null))
                progressRemaining.centerText = "Finished"
            } else {
                progressRemaining.setProgressColor(resources.getColor(R.color.primary, null))
                progressRemaining.centerText = "${(100 - percent).toInt()}%"
            }
        } else {
            progressSpent.centerText = "Set Goal"
            progressRemaining.centerText = "Set Goal"
        }

        // Categories List
        containerCategories.removeAllViews()
        for (item in data.categoryProgresses) {
            val card = layoutInflater.inflate(R.layout.item_category_progress, containerCategories, false)
            val tvCatName = card.findViewById<TextView>(R.id.textCategoryName)
            val tvCatAmount = card.findViewById<TextView>(R.id.textCategorySpent)
            val progressBar = card.findViewById<ProgressBar>(R.id.progressCategory)

            tvCatName.text = item.categoryName
            val limitValue = item.limit ?: 0.0
            tvCatAmount.text = "R ${"%.2f".format(item.spent)} / R ${"%.2f".format(limitValue)}"
            progressBar.max = limitValue.toInt().let { if (it <= 0) 100 else it }
            progressBar.progress = item.spent.toInt()

            if (limitValue > 0 && item.spent > limitValue) {
                progressBar.progressTintList = resources.getColorStateList(R.color.red, null)
                tvCatAmount.setTextColor(resources.getColor(R.color.red, null))
            }
            containerCategories.addView(card)
        }
        
        tvStats.text = "${data.categoryProgresses.size} categories tracked"
    }
}