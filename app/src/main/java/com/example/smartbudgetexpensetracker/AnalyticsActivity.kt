package com.example.smartbudgetexpensetracker

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate

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

        recyclerView.layoutManager =
            LinearLayoutManager(this)

        lifecycleScope.launch(Dispatchers.IO) {

            val database =
                AppDatabase.getDatabase(applicationContext)

            val categoryTotals =
                database.transactionDao().getExpensesByCategory()

            withContext(Dispatchers.Main) {

                val entries = categoryTotals.map {
                    PieEntry(
                        it.total.toFloat(),
                        it.category
                    )
                }

                val dataSet = PieDataSet(entries, "Expenses")
                dataSet.setColors(ColorTemplate.MATERIAL_COLORS.toList())

                val pieData = PieData(dataSet)

                pieChart.data = pieData
                pieChart.description.isEnabled = false
                pieChart.centerText = "Expenses"
                pieChart.setUsePercentValues(true)

                pieChart.legend.isEnabled = false

                pieChart.setEntryLabelColor(android.graphics.Color.DKGRAY)
                pieChart.setEntryLabelTextSize(11f)

                pieChart.setCenterTextSize(16f)

                pieChart.invalidate()

                recyclerView.adapter =
                    AnalyticsAdapter(categoryTotals)
            }
        }
    }
}