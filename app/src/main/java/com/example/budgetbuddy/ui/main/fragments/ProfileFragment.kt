package com.example.budgetbuddy.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbuddy.AppPreferences
import com.example.budgetbuddy.R
import com.example.budgetbuddy.data.database.DatabaseProvider
import com.example.budgetbuddy.data.entity.User
import com.example.budgetbuddy.ui.main.MainActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var appPreferences: AppPreferences
    private var userId: Int = -1
    private var selectedResId: Int = R.drawable.ic_default_avatar
    private var currentUser: User? = null

    companion object {
        fun newInstance(userId: Int) = ProfileFragment().apply {
            arguments = Bundle().apply { putInt("USER_ID", userId) }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        userId = arguments?.getInt("USER_ID") ?: -1
        appPreferences = AppPreferences(requireContext(), userId)

        val ivCurrentAvatar = view.findViewById<ImageView>(R.id.iv_current_avatar)
        val tvUsernameDisplay = view.findViewById<TextView>(R.id.tv_username_display)
        val rvAvatars = view.findViewById<RecyclerView>(R.id.rv_avatars)
        
        val etFirstName = view.findViewById<EditText>(R.id.et_first_name)
        val etLastName = view.findViewById<EditText>(R.id.et_last_name)
        val etAge = view.findViewById<EditText>(R.id.et_age)
        
        val ivBadge = view.findViewById<ImageView>(R.id.iv_header_badge)
        val tvBadgeTitle = view.findViewById<TextView>(R.id.tv_badge_title)

        // Load current selection
        selectedResId = appPreferences.getAvatarResId()
        ivCurrentAvatar.setImageResource(selectedResId)

        // Load user data from DB
        val db = DatabaseProvider.getDatabase(requireContext())
        lifecycleScope.launch {
            currentUser = db.userDao().getUserById(userId)
            currentUser?.let { user ->
                tvUsernameDisplay.text = "@${user.username}"
                etFirstName.setText(user.firstName ?: "")
                etLastName.setText(user.lastName ?: "")
                etAge.setText(user.age?.toString() ?: "")

                // Setup Quiz Badge from Database score
                val score = user.quizScore
                if (score >= 0) {
                    ivBadge.visibility = View.VISIBLE
                    tvBadgeTitle.visibility = View.VISIBLE
                    when {
                        score >= 90 -> {
                            ivBadge.setImageResource(R.drawable.diamond_badge)
                            tvBadgeTitle.text = "Financial Guru"
                        }
                        score >= 70 -> {
                            ivBadge.setImageResource(R.drawable.gold_medal)
                            tvBadgeTitle.text = "Budget Expert"
                        }
                        score >= 40 -> {
                            ivBadge.setImageResource(R.drawable.silver_badge)
                            tvBadgeTitle.text = "Money Smart"
                        }
                        else -> {
                            ivBadge.setImageResource(R.drawable.bronze_badge)
                            tvBadgeTitle.text = "Financial Novice"
                        }
                    }
                } else {
                    ivBadge.visibility = View.GONE
                    tvBadgeTitle.visibility = View.GONE
                }
            }
        }

        // Available Avatars
        val avatars = listOf(
            R.drawable.ic_person,
            R.drawable.ic_default_avatar,
            R.drawable.profile,
            R.drawable.profile_2,
            R.drawable.profile_3
        )

        // Setup RecyclerView
        val adapter = AvatarAdapter(avatars, selectedResId) { newResId ->
            selectedResId = newResId
            ivCurrentAvatar.setImageResource(selectedResId)
        }
        
        rvAvatars.layoutManager = GridLayoutManager(requireContext(), 3)
        rvAvatars.adapter = adapter

        view.findViewById<Button>(R.id.btn_save_profile).setOnClickListener {
            lifecycleScope.launch {
                val updatedUser = currentUser?.copy(
                    firstName = etFirstName.text.toString().trim(),
                    lastName = etLastName.text.toString().trim(),
                    age = etAge.text.toString().toIntOrNull()
                )
                
                if (updatedUser != null) {
                    db.userDao().updateUser(updatedUser)
                    currentUser = updatedUser
                }
                
                appPreferences.setAvatarResId(selectedResId)
                (activity as? MainActivity)?.updateAvatarInDrawer(selectedResId)
                
                Snackbar.make(view, "Profile Updated Successfully", Snackbar.LENGTH_SHORT).show()
            }
        }

        return view
    }
}