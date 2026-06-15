package com.example.budgetbuddy.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.budgetbuddy.AppPreferences
import com.example.budgetbuddy.R
import com.example.budgetbuddy.data.database.DatabaseProvider
import com.example.budgetbuddy.databinding.FragmentQuizBinding
import com.example.budgetbuddy.ui.main.MainActivity
import kotlinx.coroutines.launch

class QuizFragment : Fragment() {

    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!
    private lateinit var appPreferences: AppPreferences

    private var currentQuestionIndex = 0
    private var score = 0
    private var userId: Int = -1

    companion object {
        fun newInstance(userId: Int) = QuizFragment().apply {
            arguments = Bundle().apply { putInt("USER_ID", userId) }
        }
    }

    // List of multiple-choice questions for the literacy quiz
    private val questions = listOf(
        Question(
            "What is a recommended percentage of income to save each month?",
            listOf("5%", "10%", "20%", "50%"),
            2
        ),
        Question(
            "Which of these is considered a 'Fixed' expense?",
            listOf("Groceries", "Rent", "Entertainment", "Dining out"),
            1
        ),
        Question(
            "What does 'Budgeting' primarily help you do?",
            listOf("Spend more", "Avoid taxes", "Track income and expenses", "Get a loan"),
            2
        ),
        Question(
            "What is an Emergency Fund?",
            listOf("Money for vacation", "Money saved for unplanned expenses", "Credit card limit", "Retirement savings"),
            1
        ),
        Question(
            "Inflation causes the purchasing power of money to:",
            listOf("Increase", "Stay the same", "Decrease", "Double"),
            2
        )
    )

    data class Question(
        val text: String,
        val options: List<String>,
        val correctAnswerIndex: Int
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userId = arguments?.getInt("USER_ID") ?: -1
        appPreferences = AppPreferences(requireContext(), userId)
        
        displayQuestion()

        // Highlight the selected answer to show user interaction
        binding.rgOptions.setOnCheckedChangeListener { group, checkedId ->
            for (i in 0 until group.childCount) {
                val rb = group.getChildAt(i) as RadioButton
                if (rb.id == checkedId) {
                    rb.setBackgroundResource(R.drawable.circle_background)
                    rb.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
                    rb.alpha = 1.0f
                } else {
                    rb.setBackgroundResource(R.drawable.category_icon_bg)
                    rb.setTextColor(ContextCompat.getColor(requireContext(), R.color.on_surface))
                    rb.alpha = 0.6f
                }
            }
        }

        binding.btnNext.setOnClickListener {
            val selectedId = binding.rgOptions.checkedRadioButtonId
            if (selectedId == -1) {
                Toast.makeText(requireContext(), "Please select an answer", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedIndex = when (selectedId) {
                binding.rbOption1.id -> 0
                binding.rbOption2.id -> 1
                binding.rbOption3.id -> 2
                binding.rbOption4.id -> 3
                else -> -1
            }

            // Increment score if answer is correct
            if (selectedIndex == questions[currentQuestionIndex].correctAnswerIndex) {
                score++
            }

            currentQuestionIndex++

            if (currentQuestionIndex < questions.size) {
                displayQuestion()
            } else {
                showResult()
            }
        }

        binding.btnFinish.setOnClickListener {
            val percentage = (score.toFloat() / questions.size * 100).toInt()
            val finalScore = if (percentage == 0) 0 else percentage
            
            // Persist the quiz score to the database and notify the main menu
            val db = DatabaseProvider.getDatabase(requireContext())
            lifecycleScope.launch {
                val user = db.userDao().getUserById(userId)
                if (user != null) {
                    db.userDao().updateUser(user.copy(quizScore = finalScore))
                    (activity as? MainActivity)?.refreshHeader()
                }
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun displayQuestion() {
        val question = questions[currentQuestionIndex]
        binding.tvQuestionCount.text = "Question ${currentQuestionIndex + 1} of ${questions.size}"
        binding.tvQuestionText.text = question.text
        binding.rbOption1.text = question.options[0]
        binding.rbOption2.text = question.options[1]
        binding.rbOption3.text = question.options[2]
        binding.rbOption4.text = question.options[3]
        binding.rgOptions.clearCheck()
        
        // Reset option styles for the new question
        for (i in 0 until binding.rgOptions.childCount) {
            val rb = binding.rgOptions.getChildAt(i) as RadioButton
            rb.setBackgroundResource(R.drawable.category_icon_bg)
            rb.setTextColor(ContextCompat.getColor(requireContext(), R.color.on_surface))
            rb.alpha = 1.0f
        }
    }

    private fun showResult() {
        binding.tvQuestionCount.visibility = View.GONE
        binding.tvQuestionText.visibility = View.GONE
        binding.rgOptions.visibility = View.GONE
        binding.btnNext.visibility = View.GONE

        binding.llResult.visibility = View.VISIBLE
        binding.tvScore.text = "Your Score: $score / ${questions.size}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}