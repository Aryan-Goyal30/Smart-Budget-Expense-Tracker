package com.example.smartbudgetexpensetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TransactionAdapter(
    transactions: List<Transaction>
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    private val transactionList = transactions.toMutableList()

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val amount: TextView =
            itemView.findViewById(R.id.tvTransactionAmount)

        val category: TextView =
            itemView.findViewById(R.id.tvTransactionCategory)

        val description: TextView =
            itemView.findViewById(R.id.tvTransactionDescription)

        val date: TextView =
            itemView.findViewById(R.id.tvTransactionDate)

        val deleteButton: Button =
            itemView.findViewById(R.id.btnDeleteTransaction)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransactionViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)

        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: TransactionViewHolder,
        position: Int
    ) {

        val transaction = transactionList[position]

        holder.amount.text = "₹${transaction.amount}"
        holder.category.text = transaction.category
        holder.description.text = transaction.description
        holder.date.text = transaction.date

        holder.deleteButton.setOnClickListener {

            CoroutineScope(Dispatchers.IO).launch {

                val database =
                    AppDatabase.getDatabase(holder.itemView.context)

                database.transactionDao().delete(transaction)

                withContext(Dispatchers.Main) {

                    val currentPosition =
                        holder.adapterPosition

                    if (currentPosition != RecyclerView.NO_POSITION) {

                        transactionList.removeAt(currentPosition)

                        notifyItemRemoved(currentPosition)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }
}