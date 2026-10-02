package com.example.smartbudgetexpensetracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
                balanceText.text =
                    "₹%.2f".format(balance)

                incomeText.text =
                    "Income\n₹%.2f".format(income)

                expenseText.text =
                    "Expenses\n₹%.2f".format(expenses)

                // Dashboard budget information
                dashboardBudgetText.text =
                    "Monthly Budget: ₹%.2f".format(budget)

                dashboardSpentText.text =
                    "Spent This Month: ₹%.2f".format(monthlySpent)

                // Show Remaining or Over Budget
                if (remaining >= 0) {

                    dashboardRemainingText.text =
                        "Remaining: ₹%.2f".format(remaining)

                } else {

                    dashboardRemainingText.text =
                        "Over Budget: ₹%.2f".format(-remaining)
                }
            }
        }
    }
}