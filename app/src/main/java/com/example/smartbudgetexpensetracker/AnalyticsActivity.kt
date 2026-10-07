package com.example.smartbudgetexpensetracker

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.color.MaterialColors
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class AnalyticsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_analytics)

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

        val recyclerView =
            findViewById<RecyclerView>(R.id.recyclerAnalytics)

        val pieChart =
            findViewById<PieChart>(R.id.pieChart)

        val budgetDetailsLayout =
            findViewById<LinearLayout>(R.id.layoutBudgetDetails)

        val noBudgetMessage =
            findViewById<TextView>(R.id.tvNoBudgetMessage)

        val monthlyBudgetText =
            findViewById<TextView>(R.id.tvMonthlyBudget)

        val spentThisMonthText =
            findViewById<TextView>(R.id.tvSpentThisMonth)

        val remainingBudgetText =
            findViewById<TextView>(R.id.tvRemainingBudget)

        val budgetUsedText =
            findViewById<TextView>(R.id.tvBudgetUsed)

        val spendingPaceDetailsLayout =
            findViewById<LinearLayout>(R.id.layoutSpendingPaceDetails)

        val noExpensesMessage =
            findViewById<TextView>(R.id.tvNoExpensesMessage)

        val averageDailySpendingText =
            findViewById<TextView>(R.id.tvAverageDailySpending)

        val projectedMonthlySpendingText =
            findViewById<TextView>(R.id.tvProjectedMonthlySpending)

        val spendingPaceStatusText =
            findViewById<TextView>(R.id.tvSpendingPaceStatus)

        val summaryIncomeText =
            findViewById<TextView>(R.id.tvSummaryIncome)

        val summaryExpensesText =
            findViewById<TextView>(R.id.tvSummaryExpenses)

        val summarySavingsLabel =
            findViewById<TextView>(R.id.tvSummarySavingsLabel)

        val summarySavingsText =
            findViewById<TextView>(R.id.tvSummarySavings)

        val summarySavingsRateText =
            findViewById<TextView>(R.id.tvSummarySavingsRate)

        val savingsRateNote =
            findViewById<TextView>(R.id.tvSavingsRateNote)

        val budgetInsightText =
            findViewById<TextView>(R.id.tvBudgetInsight)

        val savingsInsightText =
            findViewById<TextView>(R.id.tvSavingsInsight)

        val monthComparisonDetailsLayout =
            findViewById<LinearLayout>(R.id.layoutMonthComparisonDetails)

        val noPreviousMonthText =
            findViewById<TextView>(R.id.tvNoPreviousMonthData)

        val expenseChangeText =
            findViewById<TextView>(R.id.tvExpenseChange)

        val expenseThisMonthText =
            findViewById<TextView>(R.id.tvExpenseThisMonth)

        val expenseLastMonthText =
            findViewById<TextView>(R.id.tvExpenseLastMonth)

        val expenseThisMonthBar =
            findViewById<LinearProgressIndicator>(R.id.progressExpenseThisMonth)

        val expenseLastMonthBar =
            findViewById<LinearProgressIndicator>(R.id.progressExpenseLastMonth)

        val incomeChangeText =
            findViewById<TextView>(R.id.tvIncomeChange)

        val incomeComparisonText =
            findViewById<TextView>(R.id.tvIncomeComparison)

        val savingsChangeText =
            findViewById<TextView>(R.id.tvSavingsChange)

        val savingsComparisonText =
            findViewById<TextView>(R.id.tvSavingsComparison)

        val categoryComparisonList =
            findViewById<LinearLayout>(R.id.layoutCategoryComparisonList)

        val noCategoryComparisonText =
            findViewById<TextView>(R.id.tvNoCategoryComparison)

        val dailyTrendChart =
            findViewById<LineChart>(R.id.lineChartDaily)

        val noDailyExpensesMessage =
            findViewById<TextView>(R.id.tvNoDailyExpenses)

        val budgetInsightLayout =
            findViewById<View>(R.id.layoutBudgetInsight)

        val budgetInsightDot =
            findViewById<View>(R.id.dotBudgetInsight)

        val savingsInsightLayout =
            findViewById<View>(R.id.layoutSavingsInsight)

        val savingsInsightDot =
            findViewById<View>(R.id.dotSavingsInsight)

        val budgetUsedBar =
            findViewById<LinearProgressIndicator>(R.id.progressBudgetUsed)

        val noCategoryDataMessage =
            findViewById<TextView>(R.id.tvNoCategoryData)

        // Subtitle under the title, e.g. "October 2026"
        findViewById<TextView>(R.id.tvAnalyticsSubtitle).text =
            SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())

        recyclerView.layoutManager =
            LinearLayoutManager(this)

        val preferences =
            getSharedPreferences("budget_preferences", MODE_PRIVATE)

        val monthlyBudget =
            preferences.getFloat("monthly_budget", 0f).toDouble()

        lifecycleScope.launch(Dispatchers.IO) {

            val database =
                AppDatabase.getDatabase(applicationContext)

            val categoryTotals =
                combineCategories(database.transactionDao().getExpensesByCategory())

            val calendar = java.util.Calendar.getInstance()

            val month = String.format(
                "%02d",
                calendar.get(java.util.Calendar.MONTH) + 1
            )

            val year = calendar.get(java.util.Calendar.YEAR).toString()

            val currentDayOfMonth =
                calendar.get(java.util.Calendar.DAY_OF_MONTH)

            val totalDaysInMonth =
                calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)

            val monthlyExpenses =
                database.transactionDao().getMonthlyExpenses(month, year)

            val dailyExpenseTotals =
                database.transactionDao().getDailyExpenses(month, year)

            val totalIncome =
                database.transactionDao().getTotalIncome()

            val totalExpenses =
                database.transactionDao().getTotalExpenses()

            // Previous calendar month. Calendar handles the year change,
            // so in January this gives December of the previous year.
            val previousCalendar = java.util.Calendar.getInstance()
            previousCalendar.add(java.util.Calendar.MONTH, -1)

            val previousMonth = String.format(
                "%02d",
                previousCalendar.get(java.util.Calendar.MONTH) + 1
            )

            val previousYear =
                previousCalendar.get(java.util.Calendar.YEAR).toString()

            val monthlyIncome =
                database.transactionDao().getMonthlyIncome(month, year)

            val previousMonthExpenses =
                database.transactionDao().getMonthlyExpenses(previousMonth, previousYear)

            val previousMonthIncome =
                database.transactionDao().getMonthlyIncome(previousMonth, previousYear)

            val currentCategoryTotals =
                combineCategories(database.transactionDao().getMonthlyExpensesByCategory(month, year))

            val previousCategoryTotals =
                combineCategories(database.transactionDao().getMonthlyExpensesByCategory(previousMonth, previousYear))

            withContext(Dispatchers.Main) {

                val savings = totalIncome - totalExpenses

                // Only divide when there is income, so we never divide by zero
                var savingsRate = 0.0

                if (totalIncome > 0) {
                    savingsRate = (savings / totalIncome) * 100
                }

                summaryIncomeText.text = "₹%,.2f".format(totalIncome)
                summaryExpensesText.text = "₹%,.2f".format(totalExpenses)

                if (savings < 0) {
                    summarySavingsLabel.text = "Deficit"
                    summarySavingsText.text = "₹%,.2f".format(-savings)
                    summarySavingsText.setTextColor(
                        ContextCompat.getColor(this@AnalyticsActivity, R.color.color_expense)
                    )
                } else {
                    summarySavingsLabel.text = "Savings"
                    summarySavingsText.text = "₹%,.2f".format(savings)
                }

                summarySavingsRateText.text = "%.1f%%".format(savingsRate)

                if (totalIncome > 0) {
                    savingsRateNote.visibility = View.GONE
                } else {
                    savingsRateNote.visibility = View.VISIBLE
                }

                // How Am I Doing? Simple if/else rules using values calculated above.
                // Each insight is also given a tone: good (green), warning (red) or neutral.
                // Insight 1: monthly budget
                if (monthlyBudget <= 0) {

                    budgetInsightText.text =
                        "Set a monthly budget to track your spending against a limit."
                    setInsightTone(budgetInsightLayout, budgetInsightDot, TONE_NEUTRAL)

                } else if (monthlyExpenses > monthlyBudget) {

                    budgetInsightText.text =
                        "You are over your monthly budget by ₹%,.2f."
                            .format(monthlyExpenses - monthlyBudget)
                    setInsightTone(budgetInsightLayout, budgetInsightDot, TONE_WARNING)

                } else {

                    val usedPercentage = (monthlyExpenses / monthlyBudget) * 100

                    if (usedPercentage >= 80) {
                        budgetInsightText.text =
                            "You have used %.1f%% of your monthly budget."
                                .format(usedPercentage)
                        setInsightTone(budgetInsightLayout, budgetInsightDot, TONE_WARNING)
                    } else {
                        budgetInsightText.text =
                            "You have used %.1f%% of your monthly budget and are within your limit."
                                .format(usedPercentage)
                        setInsightTone(budgetInsightLayout, budgetInsightDot, TONE_GOOD)
                    }
                }

                // Insight 2: savings
                if (totalIncome <= 0) {
                    savingsInsightText.text =
                        "Add income to see how much you are saving."
                    setInsightTone(savingsInsightLayout, savingsInsightDot, TONE_NEUTRAL)
                } else if (savings > 0) {
                    savingsInsightText.text =
                        "You are currently saving %.1f%% of your income."
                            .format(savingsRate)
                    setInsightTone(savingsInsightLayout, savingsInsightDot, TONE_GOOD)
                } else if (savings == 0.0) {
                    savingsInsightText.text =
                        "You are spending all of your income."
                    setInsightTone(savingsInsightLayout, savingsInsightDot, TONE_NEUTRAL)
                } else {
                    savingsInsightText.text =
                        "Your expenses are higher than your income."
                    setInsightTone(savingsInsightLayout, savingsInsightDot, TONE_WARNING)
                }

                if (monthlyBudget <= 0) {

                    budgetDetailsLayout.visibility = View.GONE
                    noBudgetMessage.visibility = View.VISIBLE

                } else {

                    budgetDetailsLayout.visibility = View.VISIBLE
                    noBudgetMessage.visibility = View.GONE

                    val remainingBudget = monthlyBudget - monthlyExpenses

                    val budgetUsedPercentage =
                        (monthlyExpenses / monthlyBudget) * 100

                    monthlyBudgetText.text =
                        "of ₹%,.2f".format(monthlyBudget)

                    spentThisMonthText.text =
                        "₹%,.2f".format(monthlyExpenses)

                    if (remainingBudget < 0) {
                        remainingBudgetText.text =
                            "₹%,.2f over budget".format(-remainingBudget)
                        remainingBudgetText.setTextColor(
                            ContextCompat.getColor(this@AnalyticsActivity, R.color.color_expense)
                        )
                        budgetUsedBar.setIndicatorColor(
                            ContextCompat.getColor(this@AnalyticsActivity, R.color.color_expense)
                        )
                    } else {
                        remainingBudgetText.text =
                            "₹%,.2f left".format(remainingBudget)
                        remainingBudgetText.setTextColor(
                            ContextCompat.getColor(this@AnalyticsActivity, R.color.color_income)
                        )
                        budgetUsedBar.setIndicatorColor(
                            MaterialColors.getColor(budgetUsedBar, com.google.android.material.R.attr.colorPrimary)
                        )
                    }

                    budgetUsedText.text =
                        "%.1f%% used".format(budgetUsedPercentage)

                    // The bar stops at 100% even when the budget is exceeded
                    if (budgetUsedPercentage > 100) {
                        budgetUsedBar.progress = 100
                    } else {
                        budgetUsedBar.progress = budgetUsedPercentage.toInt()
                    }
                }

                if (monthlyExpenses <= 0) {

                    spendingPaceDetailsLayout.visibility = View.GONE
                    noExpensesMessage.visibility = View.VISIBLE

                } else {

                    spendingPaceDetailsLayout.visibility = View.VISIBLE
                    noExpensesMessage.visibility = View.GONE

                    val averageDailySpending =
                        monthlyExpenses / currentDayOfMonth

                    val projectedMonthlySpending =
                        averageDailySpending * totalDaysInMonth

                    averageDailySpendingText.text =
                        "₹%,.2f".format(averageDailySpending)

                    projectedMonthlySpendingText.text =
                        "₹%,.2f".format(projectedMonthlySpending)

                    if (monthlyBudget <= 0) {

                        spendingPaceStatusText.text =
                            "Set a monthly budget to see your projected budget status."

                    } else {

                        val projectedDifference =
                            projectedMonthlySpending - monthlyBudget

                        if (projectedDifference > 0) {
                            spendingPaceStatusText.text =
                                "At your current spending rate, you may exceed your budget."
                            spendingPaceStatusText.setTextColor(
                                ContextCompat.getColor(this@AnalyticsActivity, R.color.color_expense)
                            )
                        } else {
                            spendingPaceStatusText.text =
                                "Your current spending pace is within your budget."
                            spendingPaceStatusText.setTextColor(
                                ContextCompat.getColor(this@AnalyticsActivity, R.color.color_income)
                            )
                        }
                    }
                }

                if (dailyExpenseTotals.isEmpty()) {

                    dailyTrendChart.visibility = View.GONE
                    noDailyExpensesMessage.visibility = View.VISIBLE

                } else {

                    dailyTrendChart.visibility = View.VISIBLE
                    noDailyExpensesMessage.visibility = View.GONE

                    // One slot per day: spentOnDay[1] is day 1, spentOnDay[2] is day 2, ...
                    // A new DoubleArray is filled with 0, so days without expenses stay 0.
                    val spentOnDay = DoubleArray(currentDayOfMonth + 1)

                    for (dailyTotal in dailyExpenseTotals) {

                        // date is saved as "dd/MM/yyyy", so the first part is the day
                        val day = dailyTotal.date.split("/")[0].toIntOrNull()

                        if (day != null && day >= 1 && day <= currentDayOfMonth) {
                            spentOnDay[day] = dailyTotal.total
                        }
                    }

                    // One chart point per day, from day 1 up to today
                    val trendEntries = ArrayList<Entry>()

                    for (day in 1..currentDayOfMonth) {
                        trendEntries.add(
                            Entry(day.toFloat(), spentOnDay[day].toFloat())
                        )
                    }

                    val primaryColor = MaterialColors.getColor(
                        dailyTrendChart,
                        com.google.android.material.R.attr.colorPrimary
                    )

                    val labelColor = MaterialColors.getColor(
                        dailyTrendChart,
                        com.google.android.material.R.attr.colorOnSurfaceVariant
                    )

                    val gridColor = MaterialColors.getColor(
                        dailyTrendChart,
                        com.google.android.material.R.attr.colorOutlineVariant
                    )

                    val trendDataSet = LineDataSet(trendEntries, "Daily Spending")
                    trendDataSet.color = primaryColor
                    trendDataSet.lineWidth = 2.5f
                    trendDataSet.setDrawCircles(false)
                    trendDataSet.setDrawValues(false)

                    // Straight lines between days, so the chart shows only the real daily totals
                    trendDataSet.mode = LineDataSet.Mode.LINEAR

                    // Soft fill under the line: brand colour fading to transparent
                    trendDataSet.setDrawFilled(true)
                    trendDataSet.fillDrawable = GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        intArrayOf(
                            Color.argb(70, Color.red(primaryColor), Color.green(primaryColor), Color.blue(primaryColor)),
                            Color.TRANSPARENT
                        )
                    )

                    dailyTrendChart.data = LineData(trendDataSet)
                    dailyTrendChart.description.isEnabled = false
                    dailyTrendChart.legend.isEnabled = false
                    dailyTrendChart.setTouchEnabled(false)
                    dailyTrendChart.axisRight.isEnabled = false
                    dailyTrendChart.setDrawBorders(false)
                    dailyTrendChart.extraBottomOffset = 4f

                    // X-axis: day of the month (1, 2, 3, ...), no grid lines
                    val xAxis = dailyTrendChart.xAxis
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.granularity = 1f
                    xAxis.setDrawGridLines(false)
                    xAxis.setDrawAxisLine(false)
                    xAxis.textColor = labelColor
                    xAxis.textSize = 11f
                    xAxis.yOffset = 8f

                    // Y-axis: amount spent from ₹0, a few faint dashed guide lines
                    val yAxis = dailyTrendChart.axisLeft
                    yAxis.axisMinimum = 0f
                    yAxis.setLabelCount(4, false)
                    yAxis.setDrawAxisLine(false)
                    yAxis.gridColor = gridColor
                    yAxis.enableGridDashedLine(10f, 10f, 0f)
                    yAxis.textColor = labelColor
                    yAxis.textSize = 11f
                    yAxis.xOffset = 8f
                    yAxis.valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return formatShortAmount(value)
                        }
                    }

                    dailyTrendChart.invalidate()
                }

                // Every saved amount is greater than 0, so if both of last month's
                // totals are 0, there were no transactions last month
                val hasPreviousMonthData =
                    previousMonthExpenses > 0 || previousMonthIncome > 0

                // Month comparison
                if (!hasPreviousMonthData) {

                    monthComparisonDetailsLayout.visibility = View.GONE
                    noPreviousMonthText.visibility = View.VISIBLE

                } else {

                    monthComparisonDetailsLayout.visibility = View.VISIBLE
                    noPreviousMonthText.visibility = View.GONE

                    val currentMonthSavings = monthlyIncome - monthlyExpenses
                    val previousMonthSavings = previousMonthIncome - previousMonthExpenses

                    expenseChangeText.text =
                        getChangeText(monthlyExpenses, previousMonthExpenses, true)
                    // Spending more is bad, so an increase is shown in red
                    expenseChangeText.setTextColor(
                        getChangeColor(monthlyExpenses - previousMonthExpenses, false)
                    )

                    expenseThisMonthText.text = formatMoney(monthlyExpenses)
                    expenseLastMonthText.text = formatMoney(previousMonthExpenses)

                    // Bar lengths: the bigger month fills the bar, the other is in proportion
                    var largerExpense = monthlyExpenses
                    if (previousMonthExpenses > largerExpense) {
                        largerExpense = previousMonthExpenses
                    }

                    if (largerExpense > 0) {
                        expenseThisMonthBar.progress =
                            ((monthlyExpenses / largerExpense) * 100).toInt()
                        expenseLastMonthBar.progress =
                            ((previousMonthExpenses / largerExpense) * 100).toInt()
                    } else {
                        expenseThisMonthBar.progress = 0
                        expenseLastMonthBar.progress = 0
                    }

                    incomeChangeText.text =
                        getChangeText(monthlyIncome, previousMonthIncome, true)
                    // Earning more is good, so an increase is shown in green
                    incomeChangeText.setTextColor(
                        getChangeColor(monthlyIncome - previousMonthIncome, true)
                    )
                    incomeComparisonText.text =
                        getVersusText(monthlyIncome, previousMonthIncome)

                    // No percentage for savings, because savings can be negative
                    savingsChangeText.text =
                        getChangeText(currentMonthSavings, previousMonthSavings, false)
                    savingsChangeText.setTextColor(
                        getChangeColor(currentMonthSavings - previousMonthSavings, true)
                    )
                    savingsComparisonText.text =
                        getVersusText(currentMonthSavings, previousMonthSavings)
                }

                // Category comparison
                categoryComparisonList.removeAllViews()

                if (!hasPreviousMonthData) {

                    noCategoryComparisonText.text = "No previous month data available."
                    noCategoryComparisonText.visibility = View.VISIBLE

                } else {

                    // category name -> amount spent, one map for each month
                    val currentAmounts = HashMap<String, Double>()
                    for (item in currentCategoryTotals) {
                        currentAmounts[item.category] = item.total
                    }

                    val previousAmounts = HashMap<String, Double>()
                    for (item in previousCategoryTotals) {
                        previousAmounts[item.category] = item.total
                    }

                    // Every category that appears in either month (this month's first)
                    val categoryNames = ArrayList<String>()
                    for (item in currentCategoryTotals) {
                        categoryNames.add(item.category)
                    }
                    for (item in previousCategoryTotals) {
                        if (!categoryNames.contains(item.category)) {
                            categoryNames.add(item.category)
                        }
                    }

                    if (categoryNames.isEmpty()) {

                        noCategoryComparisonText.text = "No expenses to compare yet."
                        noCategoryComparisonText.visibility = View.VISIBLE

                    } else {

                        noCategoryComparisonText.visibility = View.GONE

                        for (categoryName in categoryNames) {

                            // A category missing from a month counts as ₹0 for that month
                            val currentAmount = currentAmounts[categoryName] ?: 0.0
                            val previousAmount = previousAmounts[categoryName] ?: 0.0
                            val difference = currentAmount - previousAmount

                            // Create one row from item_category_comparison.xml
                            val row = layoutInflater.inflate(
                                R.layout.item_category_comparison,
                                categoryComparisonList,
                                false
                            )

                            row.findViewById<TextView>(R.id.tvComparisonCategory).text =
                                categoryName

                            row.findViewById<TextView>(R.id.tvComparisonAmounts).text =
                                getComparisonText(currentAmount, previousAmount)

                            // Change pill: red when spending went up, green when it went down
                            val differenceText =
                                row.findViewById<TextView>(R.id.tvComparisonDifference)

                            differenceText.text =
                                getChangeText(currentAmount, previousAmount, false)

                            val changeColor = getChangeColor(difference, false)
                            differenceText.setTextColor(changeColor)

                            if (difference > 0) {
                                differenceText.backgroundTintList = ColorStateList.valueOf(
                                    ContextCompat.getColor(this@AnalyticsActivity, R.color.expense_container)
                                )
                            } else if (difference < 0) {
                                differenceText.backgroundTintList = ColorStateList.valueOf(
                                    ContextCompat.getColor(this@AnalyticsActivity, R.color.income_container)
                                )
                            } else {
                                differenceText.backgroundTintList = ColorStateList.valueOf(
                                    MaterialColors.getColor(differenceText, com.google.android.material.R.attr.colorSurfaceContainerHigh)
                                )
                            }

                            categoryComparisonList.addView(row)
                        }
                    }
                }

                if (categoryTotals.isEmpty()) {

                    pieChart.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                    noCategoryDataMessage.visibility = View.VISIBLE

                } else {

                    pieChart.visibility = View.VISIBLE
                    recyclerView.visibility = View.VISIBLE
                    noCategoryDataMessage.visibility = View.GONE

                    val entries = categoryTotals.map {
                        PieEntry(
                            it.total.toFloat(),
                            it.category
                        )
                    }

                    // Restrained palette from colors.xml (repeats if there are more categories)
                    val chartColors = listOf(
                        ContextCompat.getColor(this@AnalyticsActivity, R.color.chart_1),
                        ContextCompat.getColor(this@AnalyticsActivity, R.color.chart_2),
                        ContextCompat.getColor(this@AnalyticsActivity, R.color.chart_3),
                        ContextCompat.getColor(this@AnalyticsActivity, R.color.chart_4),
                        ContextCompat.getColor(this@AnalyticsActivity, R.color.chart_5),
                        ContextCompat.getColor(this@AnalyticsActivity, R.color.chart_6),
                        ContextCompat.getColor(this@AnalyticsActivity, R.color.chart_7)
                    )

                    val dataSet = PieDataSet(entries, "Expenses")
                    dataSet.colors = chartColors
                    dataSet.sliceSpace = 2f
                    dataSet.setDrawValues(false)

                    pieChart.data = PieData(dataSet)
                    pieChart.description.isEnabled = false
                    pieChart.legend.isEnabled = false
                    pieChart.setDrawEntryLabels(false)
                    pieChart.setTouchEnabled(false)

                    // Donut: large hole, no see-through ring
                    pieChart.isDrawHoleEnabled = true
                    pieChart.holeRadius = 74f
                    pieChart.transparentCircleRadius = 0f
                    pieChart.setHoleColor(Color.TRANSPARENT)

                    // Centre text: total spent (bold, larger) with a small caption
                    var totalSpent = 0.0
                    for (item in categoryTotals) {
                        totalSpent += item.total
                    }

                    val amountText = formatShortAmount(totalSpent.toFloat())
                    val centerText = SpannableString(amountText + "\ntotal spent")
                    centerText.setSpan(StyleSpan(Typeface.BOLD), 0, amountText.length, 0)
                    centerText.setSpan(RelativeSizeSpan(1.6f), 0, amountText.length, 0)

                    pieChart.centerText = centerText
                    pieChart.setCenterTextSize(14f)
                    pieChart.setCenterTextColor(
                        MaterialColors.getColor(pieChart, com.google.android.material.R.attr.colorOnSurface)
                    )

                    pieChart.invalidate()

                    recyclerView.adapter =
                        AnalyticsAdapter(categoryTotals, chartColors, totalSpent)
                }
            }
        }
    }

    companion object {
        // Tones for the "How am I doing?" insights
        const val TONE_NEUTRAL = 0
        const val TONE_GOOD = 1
        const val TONE_WARNING = 2
    }

    // Display only: old records may say "food" while newer ones say "Food".
    // Shows both as "Food" (the database is not changed).
    private fun formatCategoryName(category: String): String {

        return category.trim().lowercase().replaceFirstChar { it.uppercase() }
    }

    // Adds together categories that only differ in capital letters,
    // e.g. "food" ₹500 + "Food" ₹4,190 -> "Food" ₹4,690. Biggest first.
    private fun combineCategories(categoryTotals: List<CategoryTotal>): List<CategoryTotal> {

        val combined = LinkedHashMap<String, Double>()

        for (item in categoryTotals) {
            val name = formatCategoryName(item.category)
            combined[name] = (combined[name] ?: 0.0) + item.total
        }

        val result = ArrayList<CategoryTotal>()

        for (name in combined.keys) {
            result.add(CategoryTotal(name, combined[name] ?: 0.0))
        }

        return result.sortedByDescending { it.total }
    }

    // Shows money with the minus sign before ₹, for example -₹500.00
    private fun formatMoney(amount: Double): String {

        if (amount < 0) {
            return "-₹%,.2f".format(-amount)
        }

        return "₹%,.2f".format(amount)
    }

    // For example: "₹13,790.00 this month · ₹10,000.00 last month"
    private fun getComparisonText(current: Double, previous: Double): String {

        return formatMoney(current) + " this month · " +
                formatMoney(previous) + " last month"
    }

    // For example: "₹15,000.00 vs ₹14,000.00 last month"
    private fun getVersusText(current: Double, previous: Double): String {

        return formatMoney(current) + " vs " + formatMoney(previous) + " last month"
    }

    // Short amount for chart labels, for example ₹800, ₹7K, ₹18.8K
    private fun formatShortAmount(value: Float): String {

        if (value < 1000) {
            return "₹" + value.toInt()
        }

        // Round to one decimal first, e.g. 5999.99 -> 6.0, so it shows "₹6K" not "₹6.0K"
        val thousands = Math.round(value / 100) / 10f

        if (thousands % 1 == 0f) {
            return "₹" + thousands.toInt() + "K"
        }

        return "₹%.1fK".format(thousands)
    }

    // Green for a good change, red for a bad change, grey for no change.
    // increaseIsGood is true for income and savings, false for expenses.
    private fun getChangeColor(difference: Double, increaseIsGood: Boolean): Int {

        if (difference == 0.0) {
            return MaterialColors.getColor(
                this,
                com.google.android.material.R.attr.colorOnSurfaceVariant,
                Color.GRAY
            )
        }

        val isGoodChange = (difference > 0) == increaseIsGood

        if (isGoodChange) {
            return ContextCompat.getColor(this, R.color.color_income)
        }

        return ContextCompat.getColor(this, R.color.color_expense)
    }

    // Tints an insight row and its dot: green (good), red (warning) or grey (neutral)
    private fun setInsightTone(row: View, dot: View, tone: Int) {

        if (tone == TONE_GOOD) {
            row.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.income_container)
            )
            dot.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.color_income)
            )
        } else if (tone == TONE_WARNING) {
            row.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.expense_container)
            )
            dot.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.color_expense)
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

    // For example: "↑ ₹3,790.00 (37.9%)" or "↓ ₹2,790.00"
    private fun getChangeText(
        current: Double,
        previous: Double,
        showPercentage: Boolean
    ): String {

        val difference = current - previous

        if (difference == 0.0) {
            return "No change"
        }

        val arrow = if (difference > 0) "↑" else "↓"

        var text = arrow + " ₹%,.2f".format(abs(difference))

        // Only show a percentage when last month's value is not 0,
        // so we never divide by zero
        if (showPercentage && previous != 0.0) {
            val percentage = (difference / previous) * 100
            text = text + " (%.1f%%)".format(abs(percentage))
        }

        return text
    }
}