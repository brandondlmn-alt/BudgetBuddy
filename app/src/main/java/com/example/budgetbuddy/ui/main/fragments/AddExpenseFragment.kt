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
    private var selectedStartTime: String = ""
    private var selectedEndTime: String = ""
    private var currentPhotoPath: String? = null
    private lateinit var viewModel: AddExpenseViewModel
    private var categoriesList: List<Category> = emptyList()

    private val TAG = "AddExpenseFragment"

    // 1. Contract for Camera Permission
    private val requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            prepareAndLaunchCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission is required to capture receipts", Toast.LENGTH_LONG).show()
        }
    }

    // 2. Contract for taking the picture
    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            Log.d(TAG, "Picture successfully saved to: $currentPhotoPath")
            displayThumbnail()
        } else {
            Log.w(TAG, "Camera activity returned failure or was cancelled")
            // Don't null out path immediately in case of fragment recreation issues
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

        // Restore state if fragment was recreated
        savedInstanceState?.let {
            currentPhotoPath = it.getString("photo_path")
            selectedDate = it.getString("date", "")
            selectedStartTime = it.getString("start_time", "")
            selectedEndTime = it.getString("end_time", "")
            
            if (selectedDate.isNotEmpty()) binding.tvDate.text = "Date: $selectedDate"
            displayThumbnail()
        }

        setupListeners()
        loadCategories()
    }

    private fun setupListeners() {
        binding.tvDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                selectedDate = String.format("%04d-%02d-%02d", y, m + 1, d)
                binding.tvDate.text = "Date: $selectedDate"
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnAttachReceipt.setOnClickListener {
            checkCameraPermissionAndLaunch()
        }

        binding.btnSave.setOnClickListener {
            saveTransaction()
        }
    }

    private fun checkCameraPermissionAndLaunch() {
        val permission = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            prepareAndLaunchCamera()
        } else {
            requestCameraPermission.launch(permission)
        }
    }

    private fun prepareAndLaunchCamera() {
        try {
            // Using cacheDir is usually more reliable for FileProvider sharing with external apps
            val photoFile = File.createTempFile("receipt_", ".jpg", requireContext().cacheDir)
            currentPhotoPath = photoFile.absolutePath
            
            val authority = "${requireContext().packageName}.fileprovider"
            val photoUri = FileProvider.getUriForFile(requireContext(), authority, photoFile)
            
            takePicture.launch(photoUri)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch camera: ${e.message}")
            Toast.makeText(requireContext(), "Camera error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayThumbnail() {
        if (_binding == null) return
        currentPhotoPath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                try {
                    // Downsample to avoid memory issues (crashing on high-res photos)
                    val options = BitmapFactory.Options().apply {
                        inSampleSize = 4
                    }
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
                    binding.ivReceiptThumbnail.setImageBitmap(bitmap)
                    binding.ivReceiptThumbnail.visibility = View.VISIBLE
                } catch (e: Exception) {
                    Log.e(TAG, "Error decoding image: ${e.message}")
                }
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
        Snackbar.make(binding.root, "Transaction Saved Successfully", Snackbar.LENGTH_SHORT).show()
        
        // Reset form for next entry
        binding.etAmount.text?.clear()
        binding.etDescription.text?.clear()
        binding.ivReceiptThumbnail.visibility = View.GONE
        currentPhotoPath = null
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