package com.example.smartbudgetexpensetracker

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator

// Shows one row per category under the donut chart.
// categoryColors uses the same colours as the chart slices, in the same order.
class AnalyticsAdapter(
    private val categoryTotals: List<CategoryTotal>,
    private val categoryColors: List<Int>,
    private val totalSpent: Double
) : RecyclerView.Adapter<AnalyticsAdapter.AnalyticsViewHolder>() {

    class AnalyticsViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val colorDot: View =
            itemView.findViewById(R.id.viewCategoryColor)

        val category: TextView =
            itemView.findViewById(R.id.tvAnalyticsCategory)

        val percent: TextView =
            itemView.findViewById(R.id.tvAnalyticsPercent)

        val total: TextView =
            itemView.findViewById(R.id.tvAnalyticsTotal)

        val shareBar: LinearProgressIndicator =
            itemView.findViewById(R.id.progressCategoryShare)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AnalyticsViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_analytics, parent, false)

        return AnalyticsViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: AnalyticsViewHolder,
        position: Int
    ) {
        val item = categoryTotals[position]

        // Same colour as this category's slice in the chart
        val color = categoryColors[position % categoryColors.size]

        // Share of all spending, e.g. 43.6%
        var percentage = 0.0

        if (totalSpent > 0) {
            percentage = (item.total / totalSpent) * 100
        }

        holder.category.text = item.category
        holder.total.text = "₹%,.2f".format(item.total)
        holder.percent.text = "%.1f%%".format(percentage)

        holder.colorDot.backgroundTintList = ColorStateList.valueOf(color)
        holder.shareBar.setIndicatorColor(color)
        holder.shareBar.progress = percentage.toInt()
    }

    override fun getItemCount(): Int {
        return categoryTotals.size
    }
}
