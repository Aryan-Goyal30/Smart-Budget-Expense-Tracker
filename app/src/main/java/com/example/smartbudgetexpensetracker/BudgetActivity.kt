package com.example.smartbudgetexpensetracker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.color.MaterialColors
import com.google.android.material.progressindicator.LinearProgressIndicator
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BudgetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_budget)

        val budgetText = findViewById<TextView>(R.id.tvBudget)
        val spentText = findViewById<TextView>(R.id.tvSpent)
        val remainingText = findViewById<TextView>(R.id.tvRemaining)
        val remainingLabel = findViewById<TextView>(R.id.tvRemainingLabel)
        val budgetProgress = findViewById<LinearProgressIndicator>(R.id.progressBudget)

        val budgetInput = findViewById<EditText>(R.id.etBudget)
        val saveButton = findViewById<Button>(R.id.btnSaveBudget)

        val preferences = getSharedPreferences("budget_preferences", MODE_PRIVATE)

        fun updateBudgetDisplay() {

            val budget = preferences.getFloat("monthly_budget", 0f).toDouble()

            lifecycleScope.launch(Dispatchers.IO) {

                val database = AppDatabase.getDatabase(applicationContext)

                val calendar = java.util.Calendar.getInstance()

                val month = String.format(
                    "%02d",
                    calendar.get(java.util.Calendar.MONTH) + 1
                )

                val year = calendar.get(java.util.Calendar.YEAR).toString()

                val spent = database.transactionDao().getMonthlyExpenses(month, year)

                val remaining = budget - spent

                withContext(Dispatchers.Main) {

                    spentText.text = "₹%,.2f".format(spent)

                    if (budget <= 0) {

                        budgetText.text = "Not set"
                        remainingLabel.text = "Remaining"
                        remainingText.text = "—"
                        budgetProgress.progress = 0

                    } else {

                        budgetText.text = "₹%,.2f".format(budget)

                        // Progress bar: how much of the budget is used (full bar when over)
                        var usedPercentage = ((spent / budget) * 100).toInt()

                        if (usedPercentage > 100) {
                            usedPercentage = 100
                        }

                        budgetProgress.progress = usedPercentage

                        if (remaining >= 0) {

                            remainingLabel.text = "Remaining"
                            remainingText.text = "₹%,.2f".format(remaining)
                            remainingText.setTextColor(
                                ContextCompat.getColor(this@BudgetActivity, R.color.color_income)
                            )
                            budgetProgress.setIndicatorColor(
                                MaterialColors.getColor(budgetProgress, com.google.android.material.R.attr.colorPrimary)
                            )

                        } else {

                            remainingLabel.text = "Over budget"
                            remainingText.text = "₹%,.2f".format(-remaining)
                            remainingText.setTextColor(
                                ContextCompat.getColor(this@BudgetActivity, R.color.color_expense)
                            )
                            budgetProgress.setIndicatorColor(
                                ContextCompat.getColor(this@BudgetActivity, R.color.color_expense)
                            )
                        }
                    }
                }
            }
        }

        updateBudgetDisplay()

        saveButton.setOnClickListener {

            val budgetString = budgetInput.text.toString()

            if (budgetString.isBlank()) {
                Toast.makeText(
                    this,
                    "Please enter a budget",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val budget = budgetString.toFloatOrNull()

            if (budget == null || budget < 0) {
                Toast.makeText(
                    this,
                    "Please enter a valid budget",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            preferences.edit()
                .putFloat("monthly_budget", budget)
                .apply()

            Toast.makeText(
                this,
                "Budget saved successfully!",
                Toast.LENGTH_SHORT
            ).show()

            updateBudgetDisplay()
        }

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { view, insets ->

            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }
    }
}