package com.example.budgetbuddy.ui.main.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.budgetbuddy.R
import com.example.budgetbuddy.data.entity.Category
import com.example.budgetbuddy.data.entity.Expense
import com.example.budgetbuddy.ui.main.viewmodels.AddExpenseViewModel
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.util.*

class AddExpenseFragment : Fragment() {

    private var userId: Int = -1
    private var selectedDate: String = ""
    private var selectedStartTime: String = ""
    private var selectedEndTime: String = ""
    private var photoUri: Uri? = null
    private lateinit var viewModel: AddExpenseViewModel
    private var categoriesList: List<Category> = emptyList()

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            view?.findViewById<ImageView>(R.id.iv_receipt_thumbnail)?.apply {
                setImageURI(photoUri)
                visibility = View.VISIBLE
            }
        }
    }

    companion object {
        fun newInstance(userId: Int) = AddExpenseFragment().apply {
            arguments = Bundle().apply { putInt("USER_ID", userId) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_expense, container, false)
        userId = arguments?.getInt("USER_ID") ?: -1
        viewModel = ViewModelProvider(this)[AddExpenseViewModel::class.java]

        val tvDate = view.findViewById<TextView>(R.id.tv_date)
        val tvStartTime = view.findViewById<TextView>(R.id.tv_start_time)
        val tvEndTime = view.findViewById<TextView>(R.id.tv_end_time)
        val spinnerCategory = view.findViewById<Spinner>(R.id.spinner_category)
        val etAmount = view.findViewById<EditText>(R.id.et_amount)
        val etDescription = view.findViewById<EditText>(R.id.et_description)
        val btnAttach = view.findViewById<Button>(R.id.btn_attach_receipt)
        val btnSave = view.findViewById<Button>(R.id.btn_save)

        // Observe Categories
        viewModel.loadCategories(userId)
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoriesList = categories
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories.map { it.name })
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCategory.adapter = adapter
        }

        tvDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                selectedDate = String.format("%04d-%02d-%02d", y, m+1, d)
                tvDate.text = "Date: $selectedDate"
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        tvStartTime.setOnClickListener {
            TimePickerDialog(requireContext(), { _, h, m ->
                selectedStartTime = String.format("%02d:%02d", h, m)
                tvStartTime.text = selectedStartTime
            }, 0, 0, true).show()
        }
        tvEndTime.setOnClickListener {
            TimePickerDialog(requireContext(), { _, h, m ->
                selectedEndTime = String.format("%02d:%02d", h, m)
                tvEndTime.text = selectedEndTime
            }, 0, 0, true).show()
        }

        btnAttach.setOnClickListener {
            val file = File(requireContext().filesDir, "receipt_${System.currentTimeMillis()}.jpg")
            photoUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)
            takePicture.launch(photoUri!!)
        }

        btnSave.setOnClickListener {
            val amount = etAmount.text.toString().toDoubleOrNull()
            val selectedCategoryIndex = spinnerCategory.selectedItemPosition
            
            if (amount == null || amount <= 0 || selectedDate.isEmpty() || 
                selectedStartTime.isEmpty() || selectedEndTime.isEmpty() || 
                selectedCategoryIndex < 0) {
                Snackbar.make(view, "Please fill all required fields", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val categoryId = categoriesList[selectedCategoryIndex].id

            val expense = Expense(
                userId = userId,
                categoryId = categoryId,
                amount = amount,
                date = selectedDate,
                startTime = selectedStartTime,
                endTime = selectedEndTime,
                description = etDescription.text.toString(),
                photoPath = photoUri?.toString(),
                homeCurrencyAmount = amount
            )
            viewModel.addExpense(expense)
            Snackbar.make(view, "Expense saved", Snackbar.LENGTH_SHORT).show()
            
            // Reset fields
            etAmount.text.clear()
            etDescription.text.clear()
        }

        return view
    }
}