package com.example.smartbudgetexpensetracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
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

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->

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

        val addTransactionButton =
            findViewById<Button>(R.id.btnAddTransaction)

        addTransactionButton.setOnClickListener {
            val intent =
                Intent(this, AddTransactionActivity::class.java)

            startActivity(intent)
        }

        val historyButton =
            findViewById<Button>(R.id.btnHistory)

        historyButton.setOnClickListener {
            val intent =
                Intent(this, TransactionHistoryActivity::class.java)

            startActivity(intent)
        }

        val budgetButton =
            findViewById<Button>(R.id.btnBudget)

        budgetButton.setOnClickListener {
            val intent =
                Intent(this, BudgetActivity::class.java)

            startActivity(intent)
        }

        val analyticsButton =
            findViewById<Button>(R.id.btnAnalytics)

        analyticsButton.setOnClickListener {
            val intent =
                Intent(this, AnalyticsActivity::class.java)

            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        val balanceText =
            findViewById<TextView>(R.id.tvBalance)

        val incomeText =
            findViewById<TextView>(R.id.tvIncome)

        val expenseText =
            findViewById<TextView>(R.id.tvExpense)

        val dashboardBudgetText =
            findViewById<TextView>(R.id.tvDashboardBudget)

        val dashboardSpentText =
            findViewById<TextView>(R.id.tvDashboardSpent)

        val dashboardRemainingText =
            findViewById<TextView>(R.id.tvDashboardRemaining)

        val budgetProgress =
            findViewById<LinearProgressIndicator>(R.id.progressDashboardBudget)

        lifecycleScope.launch(Dispatchers.IO) {

            val database =
                AppDatabase.getDatabase(applicationContext)

            val income =
                database.transactionDao().getTotalIncome()

            val expenses =
                database.transactionDao().getTotalExpenses()

            // Read the saved monthly budget
            val preferences =
                getSharedPreferences(
                    "budget_preferences",
                    MODE_PRIVATE
                )

            val budget =
                preferences
                    .getFloat("monthly_budget", 0f)
                    .toDouble()

            // Get the current month and year
            val calendar =
                java.util.Calendar.getInstance()

            val month =
                String.format(
                    "%02d",
                    calendar.get(java.util.Calendar.MONTH) + 1
                )

            val year =
                calendar.get(java.util.Calendar.YEAR).toString()

            // Get expenses for the current month
            val monthlySpent =
                database.transactionDao()
                    .getMonthlyExpenses(month, year)

            // Calculate balance and budget status
            val balance =
                income - expenses

            val remaining =
                budget - monthlySpent

            withContext(Dispatchers.Main) {

                // Dashboard income/expense information
                if (balance < 0) {
                    balanceText.text = "-₹%,.2f".format(-balance)
                } else {
                    balanceText.text = "₹%,.2f".format(balance)
                }

                incomeText.text =
                    "₹%,.2f".format(income)

                expenseText.text =
                    "₹%,.2f".format(expenses)

                // Dashboard budget information
                if (budget > 0) {
                    dashboardBudgetText.text = "₹%,.2f".format(budget)
                } else {
                    dashboardBudgetText.text = "Not set"
                }

                dashboardSpentText.text =
                    "₹%,.2f spent this month".format(monthlySpent)

                // Progress bar: how much of the budget is used (full bar when over)
                var usedPercentage = 0

                if (budget > 0) {
                    usedPercentage = ((monthlySpent / budget) * 100).toInt()
                }

                if (usedPercentage > 100) {
                    usedPercentage = 100
                }

                budgetProgress.progress = usedPercentage

                // Show Remaining or Over Budget
                if (budget <= 0) {

                    dashboardRemainingText.text = "Set a budget"

                    dashboardRemainingText.setTextColor(
                        MaterialColors.getColor(dashboardRemainingText, com.google.android.material.R.attr.colorPrimary)
                    )

                } else if (remaining >= 0) {

                    dashboardRemainingText.text =
                        "₹%,.2f left".format(remaining)

                    dashboardRemainingText.setTextColor(
                        ContextCompat.getColor(this@MainActivity, R.color.color_income)
                    )

                    budgetProgress.setIndicatorColor(
                        MaterialColors.getColor(budgetProgress, com.google.android.material.R.attr.colorPrimary)
                    )

                } else {

                    dashboardRemainingText.text =
                        "₹%,.2f over".format(-remaining)

                    dashboardRemainingText.setTextColor(
                        ContextCompat.getColor(this@MainActivity, R.color.color_expense)
                    )

                    budgetProgress.setIndicatorColor(
                        ContextCompat.getColor(this@MainActivity, R.color.color_expense)
                    )
                }
            }
        }
    }
}