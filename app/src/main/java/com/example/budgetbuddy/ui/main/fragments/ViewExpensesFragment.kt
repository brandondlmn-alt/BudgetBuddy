package com.example.budgetbuddy.ui.main.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbuddy.R
import com.example.budgetbuddy.data.entity.Expense
import com.example.budgetbuddy.ui.main.viewmodels.ExpenseListViewModel
import com.example.budgetbuddy.ui.main.viewmodels.ExpenseListViewModelFactory
import com.example.budgetbuddy.utils.PdfExporter
import kotlinx.coroutines.launch
import java.util.*

class ViewExpensesFragment : Fragment() {

    private var userId: Int = -1
    private lateinit var viewModel: ExpenseListViewModel
    private lateinit var rvExpenses: RecyclerView
    private lateinit var tvEmpty: TextView
    private var startDate: String = ""
    private var endDate: String = ""

    companion object {
        fun newInstance(userId: Int) = ViewExpensesFragment().apply {
            arguments = Bundle().apply { putInt("USER_ID", userId) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_expenses, container, false)
        userId = arguments?.getInt("USER_ID") ?: -1
        
        val factory = ExpenseListViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, factory)[ExpenseListViewModel::class.java]

        val tvStart = view.findViewById<TextView>(R.id.tv_start_date)
        val tvEnd = view.findViewById<TextView>(R.id.tv_end_date)
        rvExpenses = view.findViewById(R.id.rv_expenses)
        tvEmpty = view.findViewById(R.id.tv_empty)

        rvExpenses.layoutManager = LinearLayoutManager(requireContext())

        tvStart.setOnClickListener { pickDate { tvStart.text = it; startDate = it } }
        tvEnd.setOnClickListener { pickDate { tvEnd.text = it; endDate = it } }

        view.findViewById<View>(R.id.btn_apply_filter).setOnClickListener {
            if (startDate.isEmpty() || endDate.isEmpty()) {
                Toast.makeText(requireContext(), "Please select both dates", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.loadExpenses(userId, startDate, endDate)
        }

        view.findViewById<View>(R.id.btn_export_pdf).setOnClickListener { exportToPdf() }

        viewModel.expenses.observe(viewLifecycleOwner) { list ->
            rvExpenses.adapter = ExpenseAdapter(list) { expense ->
                // Handle photo click if needed
                Toast.makeText(requireContext(), "Photo: ${expense.photoPath}", Toast.LENGTH_SHORT).show()
            }
            tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        return view
    }

    private fun pickDate(onDate: (String) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, y, m, d ->
            val date = String.format("%04d-%02d-%02d", y, m+1, d)
            onDate(date)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun exportToPdf() {
        lifecycleScope.launch {
            val expenseList: List<Expense> = viewModel.expenses.value ?: emptyList()
            if (expenseList.isEmpty()) {
                Toast.makeText(requireContext(), "No expenses to export", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val exporter = PdfExporter(requireContext())
            val file = exporter.createPdf(expenseList)
            if (file != null) {
                exporter.sharePdf(file)
            } else {
                Toast.makeText(requireContext(), "Export failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}