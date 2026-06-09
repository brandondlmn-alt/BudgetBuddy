package com.example.budgetbuddy.ui.main.fragments

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.budgetbuddy.BudgetBuddyApplication
import com.example.budgetbuddy.databinding.FragmentReportsBinding
import com.example.budgetbuddy.ui.main.viewmodels.ReportsViewModel
import com.example.budgetbuddy.ui.main.views.PieChartView
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ReportsFragment : Fragment() {
    companion object {
        fun newInstance(userId: Int) = ReportsFragment().apply {
            arguments = Bundle().apply { putInt("USER_ID", userId) }
        }
    }

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReportsViewModel by viewModels()
    private val userId by lazy { requireArguments().getInt("USER_ID") }
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val today = LocalDate.now()
        binding.editStartDate.setText(today.withDayOfMonth(1).format(formatter))
        binding.editEndDate.setText(today.format(formatter))

        binding.editStartDate.setOnClickListener {
            DatePickerDialog(requireContext(), { _, y, m, d ->
                val date = LocalDate.of(y, m + 1, d)
                binding.editStartDate.setText(date.format(formatter))
            }, today.year, today.monthValue - 1, today.dayOfMonth).show()
        }

        binding.editEndDate.setOnClickListener {
            DatePickerDialog(requireContext(), { _, y, m, d ->
                val date = LocalDate.of(y, m + 1, d)
                binding.editEndDate.setText(date.format(formatter))
            }, today.year, today.monthValue - 1, today.dayOfMonth).show()
        }

        binding.btnFilter.setOnClickListener {
            val start = binding.editStartDate.text.toString()
            val end = binding.editEndDate.text.toString()
            viewModel.loadTotals(userId, start, end)
            updateGoalReference(start)
        }

        viewModel.categoryTotals.observe(viewLifecycleOwner) { list ->
            val totalOverall = list.sumOf { it.second }
            binding.textTotalOverall.text = "Total: R ${String.format("%.2f", totalOverall)}"

            val slices = mutableListOf<PieChartView.Slice>()
            val breakdown = StringBuilder()

            list.forEach { (category, amount) ->
                if (amount > 0) {
                    val color = try {
                        Color.parseColor(category.colorCode)
                    } catch (e: Exception) {
                        Color.GRAY
                    }
                    slices.add(PieChartView.Slice(category.name, amount.toFloat(), color))
                    breakdown.append("${category.name}: R ${String.format("%.2f", amount)}\n")
                }
            }

            binding.pieChartView.slices = slices
            binding.textTotals.text = if (breakdown.isEmpty()) "No data for this period." else breakdown.toString()
        }

        binding.btnFilter.performClick()
    }

    private fun updateGoalReference(startDate: String) {
        val monthKey = startDate.substring(0, 7) // Extract YYYY-MM
        val db = (requireActivity().application as BudgetBuddyApplication).database
        
        lifecycleScope.launch {
            val goal = db.goalDao().getGoalForUserAndMonth(userId, monthKey)
            goal?.let {
                val goalText = "Monthly Goal: R ${it.minAmount} (Min) - R ${it.maxAmount} (Max)"
                // Dynamically update the summary with Goal info to satisfy Requirement #4
                binding.textTotalOverall.append("\n\n$goalText")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}