package com.example.smartbudgetexpensetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AnalyticsAdapter(
    private val categoryTotals: List<CategoryTotal>
) : RecyclerView.Adapter<AnalyticsAdapter.AnalyticsViewHolder>() {

    class AnalyticsViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val category: TextView =
            itemView.findViewById(R.id.tvAnalyticsCategory)

        val total: TextView =
            itemView.findViewById(R.id.tvAnalyticsTotal)
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

        holder.category.text = item.category
        holder.total.text = "₹%.2f".format(item.total)
    }

    override fun getItemCount(): Int {
        return categoryTotals.size
    }
}