package com.example.budgetbuddy.ui.main.fragments

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbuddy.R
import com.example.budgetbuddy.data.entity.Expense
import java.io.File

class ExpenseAdapter(
    val items: List<Expense>,
    private val onPhotoClick: (Expense) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textSummary: TextView = view.findViewById(R.id.textExpenseSummary)
        val textCategory: TextView = view.findViewById(R.id.textCategory)
        val textAmount: TextView = view.findViewById(R.id.textAmount)
        val ivReceipt: ImageView = view.findViewById(R.id.iv_receipt_preview)
        val viewCategoryColor: View = view.findViewById(R.id.viewCategoryColor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val e = items[position]
        
        holder.textSummary.text = e.description
        holder.textCategory.text = "${e.date} • Category ID: ${e.categoryId}" 
        holder.textAmount.text = "R ${String.format("%.2f", e.amount)}"

        // Display Receipt Thumbnail with reliability
        if (!e.photoPath.isNullOrEmpty()) {
            val imgFile = File(e.photoPath)
            if (imgFile.exists()) {
                holder.ivReceipt.visibility = View.VISIBLE
                // Use a smaller sample size to avoid memory issues in the list
                val options = BitmapFactory.Options().apply {
                    inSampleSize = 4 
                }
                val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath, options)
                holder.ivReceipt.setImageBitmap(bitmap)
            } else {
                holder.ivReceipt.visibility = View.GONE
            }
        } else {
            holder.ivReceipt.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            onPhotoClick(e)
        }
    }

    override fun getItemCount() = items.size
}