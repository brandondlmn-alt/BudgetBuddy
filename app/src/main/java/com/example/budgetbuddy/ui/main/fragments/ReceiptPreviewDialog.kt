package com.example.budgetbuddy.ui.main.fragments

import android.app.Dialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Window
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.example.budgetbuddy.R
import java.io.File

class ReceiptPreviewDialog(private val photoPath: String) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_receipt_preview)
        
        val imageView = dialog.findViewById<ImageView>(R.id.iv_full_receipt)
        val file = File(photoPath)
        if (file.exists()) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            imageView.setImageBitmap(bitmap)
        }
        
        imageView.setOnClickListener {
            dismiss()
        }
        
        return dialog
    }
}