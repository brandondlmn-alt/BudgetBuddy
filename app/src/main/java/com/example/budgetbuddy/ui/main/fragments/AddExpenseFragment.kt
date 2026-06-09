package com.example.budgetbuddy.ui.main.fragments

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.budgetbuddy.R
import com.example.budgetbuddy.data.entity.Category
import com.example.budgetbuddy.data.entity.Expense
import com.example.budgetbuddy.databinding.FragmentAddExpenseBinding
import com.example.budgetbuddy.ui.main.viewmodels.AddExpenseViewModel
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.util.*

class AddExpenseFragment : Fragment() {

    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!
    
    private var userId: Int = -1
    private var selectedDate: String = ""
    private var selectedStartTime: String = "00:00"
    private var selectedEndTime: String = "23:59"
    private var currentPhotoPath: String? = null
    private lateinit var viewModel: AddExpenseViewModel
    private var categoriesList: List<Category> = emptyList()

    private val FILE_PROVIDER_AUTHORITY = "com.example.budgetbuddy.fileprovider"

    private val requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission is required to capture receipts", Toast.LENGTH_LONG).show()
        }
    }

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            displayThumbnail()
        } else {
            currentPhotoPath = null
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
    ): View {
        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userId = arguments?.getInt("USER_ID") ?: -1
        viewModel = ViewModelProvider(this)[AddExpenseViewModel::class.java]

        // Restore state
        savedInstanceState?.let {
            currentPhotoPath = it.getString("photo_path")
            selectedDate = it.getString("date", "")
            selectedStartTime = it.getString("start_time", "00:00")
            selectedEndTime = it.getString("end_time", "23:59")
        }

        updateDateTimeUI()
        displayThumbnail()
        
        setupListeners()
        loadCategories()
    }

    private fun updateDateTimeUI() {
        binding.tvDate.text = if (selectedDate.isNotEmpty()) "Date: $selectedDate" else "Select Date"
        binding.tvStartTime.text = "Start: $selectedStartTime"
        binding.tvEndTime.text = "End: $selectedEndTime"
    }

    private fun setupListeners() {
        binding.tvDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                selectedDate = String.format("%04d-%02d-%02d", y, m + 1, d)
                binding.tvDate.text = "Date: $selectedDate"
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.tvStartTime.setOnClickListener {
            val h = try { selectedStartTime.split(":")[0].toInt() } catch(e: Exception) { 0 }
            val m = try { selectedStartTime.split(":")[1].toInt() } catch(e: Exception) { 0 }
            
            TimePickerDialog(requireContext(), { _, hours, minutes ->
                selectedStartTime = String.format("%02d:%02d", hours, minutes)
                binding.tvStartTime.text = "Start: $selectedStartTime"
            }, h, m, true).show()
        }

        binding.tvEndTime.setOnClickListener {
            val h = try { selectedEndTime.split(":")[0].toInt() } catch(e: Exception) { 23 }
            val m = try { selectedEndTime.split(":")[1].toInt() } catch(e: Exception) { 59 }

            TimePickerDialog(requireContext(), { _, hours, minutes ->
                selectedEndTime = String.format("%02d:%02d", hours, minutes)
                binding.tvEndTime.text = "End: $selectedEndTime"
            }, h, m, true).show()
        }

        binding.btnAttachReceipt.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                launchCamera()
            } else {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
        }

        binding.btnSave.setOnClickListener {
            saveTransaction()
        }
    }

    private fun launchCamera() {
        try {
            val photoFile = File.createTempFile("receipt_", ".jpg", requireContext().cacheDir)
            currentPhotoPath = photoFile.absolutePath
            val photoUri = FileProvider.getUriForFile(requireContext(), FILE_PROVIDER_AUTHORITY, photoFile)
            takePicture.launch(photoUri)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error starting camera", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayThumbnail() {
        if (_binding == null) return
        currentPhotoPath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                val options = BitmapFactory.Options().apply { inSampleSize = 4 }
                val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
                binding.ivReceiptThumbnail.setImageBitmap(bitmap)
                binding.ivReceiptThumbnail.visibility = View.VISIBLE
            }
        }
    }

    private fun loadCategories() {
        viewModel.loadCategories(userId)
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoriesList = categories
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories.map { it.name })
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCategory.adapter = adapter
        }
    }

    private fun saveTransaction() {
        val amount = binding.etAmount.text.toString().toDoubleOrNull()
        val categoryIndex = binding.spinnerCategory.selectedItemPosition
        
        if (amount == null || amount <= 0 || selectedDate.isEmpty() || categoryIndex < 0) {
            Snackbar.make(binding.root, "Please complete all fields (Amount, Date, Category)", Snackbar.LENGTH_SHORT).show()
            return
        }

        val expense = Expense(
            userId = userId,
            categoryId = categoriesList[categoryIndex].id,
            amount = amount,
            date = selectedDate,
            startTime = selectedStartTime,
            endTime = selectedEndTime,
            description = binding.etDescription.text.toString(),
            photoPath = currentPhotoPath,
            homeCurrencyAmount = amount
        )
        
        viewModel.addExpense(expense)
        Snackbar.make(binding.root, "Expense Saved Successfully", Snackbar.LENGTH_SHORT).show()
        
        // Reset form
        binding.etAmount.text?.clear()
        binding.etDescription.text?.clear()
        binding.ivReceiptThumbnail.visibility = View.GONE
        currentPhotoPath = null
        selectedDate = ""
        selectedStartTime = "00:00"
        selectedEndTime = "23:59"
        updateDateTimeUI()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("photo_path", currentPhotoPath)
        outState.putString("date", selectedDate)
        outState.putString("start_time", selectedStartTime)
        outState.putString("end_time", selectedEndTime)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}