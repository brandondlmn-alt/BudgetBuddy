package com.example.budgetbuddy.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.budgetbuddy.BudgetBuddyApplication
import com.example.budgetbuddy.R
import com.example.budgetbuddy.databinding.FragmentGamificationBinding
import kotlinx.coroutines.launch

class GamificationFragment : Fragment() {
    companion object {
        fun newInstance(userId: Int) = GamificationFragment().apply {
            arguments = Bundle().apply { putInt("USER_ID", userId) }
        }
    }

    private var _binding: FragmentGamificationBinding? = null
    private val binding get() = _binding!!
    private val userId by lazy { requireArguments().getInt("USER_ID") }
    private val db get() = (requireActivity().application as BudgetBuddyApplication).database

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGamificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.btnQuiz.setOnClickListener {
            val quizFragment = QuizFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, quizFragment)
                .addToBackStack(null)
                .commit()
        }

        lifecycleScope.launch {
            val totalExp = db.expenseDao().getTotalExpenseCount(userId)

            // Bronze Tier: 1 Expense
            binding.progressBronze.progress = if (totalExp >= 1) 1 else 0
            if (totalExp >= 1) {
                binding.badgeBronze.alpha = 1.0f
                binding.tvBronzeStatus.text = "Bronze Tier: Achieved!"
            } else {
                binding.tvBronzeStatus.text = "Log 1 expense to unlock"
            }

            // Silver Tier: 10 Expenses
            binding.progressSilver.progress = totalExp.coerceAtMost(10)
            if (totalExp >= 10) {
                binding.badgeSilver.alpha = 1.0f
                binding.tvSilverStatus.text = "Silver Tier: Achieved!"
            } else {
                binding.tvSilverStatus.text = "Log $totalExp/10 expenses"
            }

            // Gold Tier: 50 Expenses
            binding.progressGold.progress = totalExp.coerceAtMost(50)
            if (totalExp >= 50) {
                binding.badgeGold.alpha = 1.0f
                binding.tvGoldStatus.text = "Gold Tier: Achieved!"
            } else {
                binding.tvGoldStatus.text = "Log $totalExp/50 expenses"
            }

            // Platinum Tier: 100 Expenses
            binding.progressPlatinum.progress = totalExp.coerceAtMost(100)
            if (totalExp >= 100) {
                binding.badgePlatinum.alpha = 1.0f
                binding.tvPlatinumStatus.text = "Platinum Tier: Achieved!"
            } else {
                binding.tvPlatinumStatus.text = "Log $totalExp/100 expenses"
            }

            // Diamond Tier: 250 Expenses
            binding.progressDiamond.progress = totalExp.coerceAtMost(250)
            if (totalExp >= 250) {
                binding.badgeDiamond.alpha = 1.0f
                binding.tvDiamondStatus.text = "Diamond Tier: Achieved!"
            } else {
                binding.tvDiamondStatus.text = "Log $totalExp/250 expenses"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
