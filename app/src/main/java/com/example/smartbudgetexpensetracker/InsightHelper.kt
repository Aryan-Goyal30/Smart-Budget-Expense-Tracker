package com.example.smartbudgetexpensetracker

import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.color.MaterialColors

object InsightHelper {

    const val TONE_NEUTRAL = 0
    const val TONE_GOOD = 1
    const val TONE_WARNING = 2

    data class InsightResult(
        val message: String,
        val tone: Int
    )

    fun getBudgetInsight(monthlyBudget: Double, monthlyExpenses: Double): InsightResult {
        return if (monthlyBudget <= 0) {
            InsightResult(
                "Set a monthly budget to track your spending against a limit.",
                TONE_NEUTRAL
            )
        } else if (monthlyExpenses > monthlyBudget) {
            InsightResult(
                "You are over your monthly budget by ₹%,.2f.".format(monthlyExpenses - monthlyBudget),
                TONE_WARNING
            )
        } else {
            val usedPercentage = (monthlyExpenses / monthlyBudget) * 100
            if (usedPercentage >= 80) {
                InsightResult(
                    "You have used %.1f%% of your monthly budget.".format(usedPercentage),
                    TONE_WARNING
                )
            } else {
                InsightResult(
                    "You have used %.1f%% of your monthly budget and are within your limit.".format(usedPercentage),
                    TONE_GOOD
                )
            }
        }
    }

    fun getSavingsInsight(totalIncome: Double, savings: Double, isAllTime: Boolean): InsightResult {
        val period = if (isAllTime) "all-time" else "monthly"
        return if (totalIncome <= 0) {
            InsightResult(
                "Add income to see your $period savings.",
                TONE_NEUTRAL
            )
        } else if (savings > 0) {
            val savingsRate = (savings / totalIncome) * 100
            InsightResult(
                "Your $period savings rate is %.1f%% of your income.".format(savingsRate),
                TONE_GOOD
            )
        } else if (savings == 0.0) {
            InsightResult(
                "You are spending all of your $period income.",
                TONE_NEUTRAL
            )
        } else {
            InsightResult(
                "Your $period expenses are higher than your income.",
                TONE_WARNING
            )
        }
    }

    fun setInsightTone(context: Context, row: View, dot: View, tone: Int) {
        if (tone == TONE_GOOD) {
            row.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.income_container)
            )
            dot.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.color_income)
            )
        } else if (tone == TONE_WARNING) {
            row.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.expense_container)
            )
            dot.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.color_expense)
            )
        } else {
            row.backgroundTintList = ColorStateList.valueOf(
                MaterialColors.getColor(row, com.google.android.material.R.attr.colorSurfaceContainer)
            )
            dot.backgroundTintList = ColorStateList.valueOf(
                MaterialColors.getColor(dot, com.google.android.material.R.attr.colorOutline)
            )
        }
    }
}