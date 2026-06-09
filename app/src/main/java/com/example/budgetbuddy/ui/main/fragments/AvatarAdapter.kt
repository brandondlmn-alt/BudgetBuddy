package com.example.budgetbuddy.ui.main.fragments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbuddy.databinding.ItemAvatarBinding

class AvatarAdapter(
    private val avatars: List<Int>,
    private var selectedResId: Int,
    private val onAvatarSelected: (Int) -> Unit
) : RecyclerView.Adapter<AvatarAdapter.ViewHolder>() {

    // Use ViewBinding for cleaner and safer view access
    class ViewHolder(val binding: ItemAvatarBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAvatarBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val resId = avatars[position]
        holder.binding.ivAvatarItem.setImageResource(resId)

        // Highlight the currently selected avatar
        if (resId == selectedResId) {
            holder.binding.cardAvatar.strokeWidth = 6
            holder.binding.cardAvatar.alpha = 1.0f
        } else {
            holder.binding.cardAvatar.strokeWidth = 0
            holder.binding.cardAvatar.alpha = 0.6f
        }

        holder.itemView.setOnClickListener {
            if (resId != selectedResId) {
                val previousSelectedId = selectedResId
                selectedResId = resId

                // Update only the items that changed for better performance
                val previousIndex = avatars.indexOf(previousSelectedId)
                if (previousIndex != -1) {
                    notifyItemChanged(previousIndex)
                }
                notifyItemChanged(position)

                onAvatarSelected(resId)
            }
        }
    }

    override fun getItemCount(): Int = avatars.size
}