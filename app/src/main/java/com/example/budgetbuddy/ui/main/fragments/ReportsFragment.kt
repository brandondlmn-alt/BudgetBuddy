package com.example.budgetbuddy.ui.main.fragments

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.budgetbuddy.databinding.FragmentReportsBinding
import com.example.budgetbuddy.ui.main.viewmodels.ReportsViewModel
import com.example.budgetbuddy.ui.main.views.PieChartView
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
            viewModel.loadTotals(userId, binding.editStartDate.text.toString(), binding.editEndDate.text.toString())
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
            binding.textTotals.text = if (breakdown.isEmpty()) "No data available for this period." else breakdown.toString()
        }

        binding.btnFilter.performClick()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}