package com.example.budgetbuddy.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbuddy.AppPreferences
import com.example.budgetbuddy.R
import com.example.budgetbuddy.data.database.DatabaseProvider
import com.example.budgetbuddy.ui.main.MainActivity
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var appPreferences: AppPreferences
    private var userId: Int = -1
    private var selectedResId: Int = R.drawable.ic_default_avatar

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
        val tvProfileName = view.findViewById<TextView>(R.id.tv_profile_name)
        val rvAvatars = view.findViewById<RecyclerView>(R.id.rv_avatars)
        
        // Badge Views
        val llBadge = view.findViewById<LinearLayout>(R.id.ll_quiz_badge)
        val ivBadge = view.findViewById<ImageView>(R.id.iv_quiz_badge)
        val tvBadgeTitle = view.findViewById<TextView>(R.id.tv_badge_title)

        // Load current selection
        selectedResId = appPreferences.getAvatarResId()
        ivCurrentAvatar.setImageResource(selectedResId)

        // Load username from DB
        val db = DatabaseProvider.getDatabase(requireContext())
        lifecycleScope.launch {
            val user = db.userDao().getUserById(userId)
            tvProfileName.text = user?.username ?: "User"
        }

        // Setup Quiz Badge
        val score = appPreferences.getQuizScore()
        if (score > 0) {
            llBadge.visibility = View.VISIBLE
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
            llBadge.visibility = View.GONE
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
            appPreferences.setAvatarResId(selectedResId)
            (activity as? MainActivity)?.updateAvatarInDrawer(selectedResId)
            parentFragmentManager.popBackStack()
        }

        return view
    }
}