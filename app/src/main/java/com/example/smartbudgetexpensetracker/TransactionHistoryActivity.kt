package com.example.smartbudgetexpensetracker

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TransactionHistoryActivity : AppCompatActivity() {

    // Every transaction loaded from Room.
    // The RecyclerView shows a searched, filtered and sorted copy of this list.
    private var allTransactions = mutableListOf<Transaction>()

    private val sortOptions = arrayOf(
        "Newest first",
        "Oldest first",
        "Highest amount",
        "Lowest amount"
    )

    private val monthNames = arrayOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchInput: EditText
    private lateinit var typeChipGroup: ChipGroup
    private lateinit var sortInput: MaterialAutoCompleteTextView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var emptyTitleText: TextView
    private lateinit var emptyMessageText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_transaction_history)

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

        recyclerView =
            findViewById(R.id.recyclerTransactions)

        recyclerView.layoutManager = LinearLayoutManager(this)

        searchInput = findViewById(R.id.etSearch)
        typeChipGroup = findViewById(R.id.chipGroupType)
        sortInput = findViewById(R.id.actvSort)
        emptyStateLayout = findViewById(R.id.layoutEmptyState)
        emptyTitleText = findViewById(R.id.tvEmptyTitle)
        emptyMessageText = findViewById(R.id.tvEmptyMessage)

        sortInput.setSimpleItems(sortOptions)
        sortInput.setText(sortOptions[0], false)

        // Update the list whenever the search text, filter or sort option changes
        searchInput.doAfterTextChanged {
            showTransactions()
        }

        typeChipGroup.setOnCheckedStateChangeListener { _, _ ->
            showTransactions()
        }

        sortInput.setOnItemClickListener { _, _, _, _ ->
            showTransactions()
        }
    }

    override fun onResume() {
        super.onResume()

        // Load in onResume (not onCreate) so the list is refreshed
        // when the user comes back after editing a transaction
        lifecycleScope.launch(Dispatchers.IO) {

            val database = AppDatabase.getDatabase(applicationContext)

            val transactions =
                database.transactionDao().getAllTransactions()

            withContext(Dispatchers.Main) {

                allTransactions = transactions.toMutableList()

                showTransactions()
            }
        }
    }

    private fun showTransactions() {

        val searchText = searchInput.text.toString().trim()

        val selectedType = when (typeChipGroup.checkedChipId) {
            R.id.chipIncome -> "Income"
            R.id.chipExpense -> "Expense"
            else -> "All"
        }

        // Search and filter: keep only the transactions that match both
        val filteredList = allTransactions.filter { transaction ->

            val matchesSearch = searchText.isEmpty() ||
                    transaction.category.contains(searchText, ignoreCase = true) ||
                    transaction.description.contains(searchText, ignoreCase = true)

            val matchesType = selectedType == "All" ||
                    transaction.type == selectedType

            matchesSearch && matchesType
        }

        // Sort: id is auto-generated, so a higher id means the transaction was added later
        val sortedList = when (sortInput.text.toString()) {
            "Oldest first" -> filteredList.sortedBy { it.id }
            "Highest amount" -> filteredList.sortedByDescending { it.amount }
            "Lowest amount" -> filteredList.sortedBy { it.amount }
            else -> filteredList.sortedByDescending { it.id }
        }

        // Group by month (after search, filter and sort).
        // Months are newest first, or oldest first when "Oldest first" is chosen.
        // sortedBy keeps the existing order inside each month, so the chosen sort still applies.
        val monthOrderedList = if (sortInput.text.toString() == "Oldest first") {
            sortedList.sortedBy { getMonthNumber(it.date) }
        } else {
            sortedList.sortedByDescending { getMonthNumber(it.date) }
        }

        // Add a month header (a String) before the first transaction of each month
        val groupedList = ArrayList<Any>()
        var previousMonthKey = ""

        for (transaction in monthOrderedList) {

            // "06/10/2026" -> "10/2026"
            val monthKey = transaction.date.substringAfter("/")

            if (monthKey != previousMonthKey) {
                groupedList.add(getMonthTitle(monthKey))
                previousMonthKey = monthKey
            }

            groupedList.add(transaction)
        }

        recyclerView.adapter = TransactionAdapter(groupedList) { deletedTransaction ->

            // Remove it from the full list too, so it does not come back
            // when the search, filter or sort changes
            allTransactions.remove(deletedTransaction)

            updateEmptyState(recyclerView.adapter?.itemCount ?: 0)
        }

        updateEmptyState(sortedList.size)
    }

    // "06/10/2026" -> 202610, so months can be sorted as numbers (year first, then month)
    private fun getMonthNumber(date: String): Int {

        val parts = date.split("/")

        if (parts.size < 3) {
            return 0
        }

        val month = parts[1].toIntOrNull() ?: 0
        val year = parts[2].toIntOrNull() ?: 0

        return year * 100 + month
    }

    // "10/2026" -> "October 2026"
    private fun getMonthTitle(monthKey: String): String {

        val parts = monthKey.split("/")
        val month = parts[0].toIntOrNull()

        if (parts.size < 2 || month == null || month < 1 || month > 12) {
            return monthKey
        }

        return monthNames[month - 1] + " " + parts[1]
    }

    private fun updateEmptyState(visibleCount: Int) {

        if (visibleCount == 0) {

            recyclerView.visibility = View.GONE
            emptyStateLayout.visibility = View.VISIBLE

            if (allTransactions.isEmpty()) {
                emptyTitleText.text = "No transactions yet"
                emptyMessageText.text =
                    "Add your first income or expense\nto start tracking your finances."
            } else {
                emptyTitleText.text = "No matching transactions"
                emptyMessageText.text =
                    "Try a different search term\nor change the filter."
            }

        } else {

            recyclerView.visibility = View.VISIBLE
            emptyStateLayout.visibility = View.GONE
        }
    }
}
