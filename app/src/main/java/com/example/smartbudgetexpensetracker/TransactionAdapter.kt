package com.example.smartbudgetexpensetracker

import android.content.Intent
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// The list holds two kinds of items:
// a String is a month header (e.g. "October 2026"), a Transaction is a transaction card
class TransactionAdapter(
    items: List<Any>,
    private val onTransactionDeleted: (Transaction) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val itemList = items.toMutableList()

    companion object {
        const val TYPE_MONTH_HEADER = 0
        const val TYPE_TRANSACTION = 1
    }

    class MonthHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val monthTitle: TextView =
            itemView.findViewById(R.id.tvMonthHeader)
    }

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val icon: TextView =
            itemView.findViewById(R.id.tvTransactionIcon)

        val amount: TextView =
            itemView.findViewById(R.id.tvTransactionAmount)

        val category: TextView =
            itemView.findViewById(R.id.tvTransactionCategory)

        val description: TextView =
            itemView.findViewById(R.id.tvTransactionDescription)

        val date: TextView =
            itemView.findViewById(R.id.tvTransactionDate)

        val editButton: Button =
            itemView.findViewById(R.id.btnEditTransaction)

        val deleteButton: Button =
            itemView.findViewById(R.id.btnDeleteTransaction)
    }

    // Tells the RecyclerView which layout each position needs
    override fun getItemViewType(position: Int): Int {

        if (itemList[position] is String) {
            return TYPE_MONTH_HEADER
        }

        return TYPE_TRANSACTION
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        if (viewType == TYPE_MONTH_HEADER) {

            val view = inflater.inflate(R.layout.item_month_header, parent, false)

            return MonthHeaderViewHolder(view)
        }

        val view = inflater.inflate(R.layout.item_transaction, parent, false)

        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {

        if (holder is MonthHeaderViewHolder) {
            holder.monthTitle.text = itemList[position] as String
            return
        }

        val transactionHolder = holder as TransactionViewHolder

        val transaction = itemList[position] as Transaction

        val context = transactionHolder.itemView.context

        // Display only: shows old "food" records as "Food" (the database is not changed)
        transactionHolder.category.text =
            transaction.category.trim().lowercase().replaceFirstChar { it.uppercase() }
        transactionHolder.date.text = transaction.date

        // Shown after the date as "  ·  description" (hidden when empty)
        if (transaction.description.isBlank()) {
            transactionHolder.description.visibility = View.GONE
        } else {
            transactionHolder.description.visibility = View.VISIBLE
            transactionHolder.description.text = "  ·  " + transaction.description
        }

        // First letter of the category inside the circle
        transactionHolder.icon.text = transaction.category.take(1).uppercase()

        // Income: "+₹" in green with a green circle.
        // Expense: "−₹" in the normal text colour with a red circle.
        if (transaction.type == "Income") {

            transactionHolder.amount.text = "+₹%,.2f".format(transaction.amount)
            transactionHolder.amount.setTextColor(
                ContextCompat.getColor(context, R.color.color_income)
            )
            transactionHolder.icon.setTextColor(
                ContextCompat.getColor(context, R.color.color_income)
            )
            transactionHolder.icon.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.income_container)
            )

        } else {

            transactionHolder.amount.text = "−₹%,.2f".format(transaction.amount)
            transactionHolder.amount.setTextColor(
                MaterialColors.getColor(transactionHolder.amount, com.google.android.material.R.attr.colorOnSurface)
            )
            transactionHolder.icon.setTextColor(
                ContextCompat.getColor(context, R.color.color_expense)
            )
            transactionHolder.icon.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.expense_container)
            )
        }

        transactionHolder.editButton.setOnClickListener {

            // Open the Add Transaction screen in edit mode for this transaction
            val intent = Intent(context, AddTransactionActivity::class.java)
            intent.putExtra("transaction_id", transaction.id)

            context.startActivity(intent)
        }

        transactionHolder.deleteButton.setOnClickListener {

            CoroutineScope(Dispatchers.IO).launch {

                val database =
                    AppDatabase.getDatabase(transactionHolder.itemView.context)

                database.transactionDao().delete(transaction)

                withContext(Dispatchers.Main) {

                    val currentPosition =
                        transactionHolder.adapterPosition

                    if (currentPosition != RecyclerView.NO_POSITION) {

                        itemList.removeAt(currentPosition)

                        notifyItemRemoved(currentPosition)

                        removeEmptyMonthHeader(currentPosition - 1)
                    }

                    // Let the History screen remove it from its full list too
                    onTransactionDeleted(transaction)
                }
            }
        }
    }

    // If a month header no longer has any transactions under it, remove the header
    private fun removeEmptyMonthHeader(headerPosition: Int) {

        if (headerPosition < 0 || itemList[headerPosition] !is String) {
            return
        }

        val nextPosition = headerPosition + 1

        val monthIsEmpty =
            nextPosition >= itemList.size || itemList[nextPosition] is String

        if (monthIsEmpty) {
            itemList.removeAt(headerPosition)
            notifyItemRemoved(headerPosition)
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}
