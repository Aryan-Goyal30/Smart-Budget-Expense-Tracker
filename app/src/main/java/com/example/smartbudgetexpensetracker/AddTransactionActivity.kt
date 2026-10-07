package com.example.smartbudgetexpensetracker

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddTransactionActivity : AppCompatActivity() {

    private val expenseCategories = arrayOf(
        "Food",
        "Shopping",
        "Transport",
        "Bills",
        "Entertainment",
        "Education",
        "Health",
        "Travel",
        "Other"
    )

    private val incomeCategories = arrayOf(
        "Salary",
        "Freelance",
        "Scholarship",
        "Gift",
        "Other"
    )

    // The transaction being edited. It stays null when adding a new transaction.
    private var transactionToEdit: Transaction? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_add_transaction)

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

        val titleText = findViewById<TextView>(R.id.tvAddTransactionTitle)

        val amountInput = findViewById<EditText>(R.id.etAmount)
        val categoryInput = findViewById<MaterialAutoCompleteTextView>(R.id.etCategory)
        val descriptionInput = findViewById<EditText>(R.id.etDescription)
        val dateInput = findViewById<EditText>(R.id.etDate)
        val dateLayout = findViewById<TextInputLayout>(R.id.tilDate)

        val typeRadioGroup = findViewById<RadioGroup>(R.id.rgTransactionType)
        val expenseRadioButton = findViewById<RadioButton>(R.id.rbExpense)
        val incomeRadioButton = findViewById<RadioButton>(R.id.rbIncome)
        val saveButton = findViewById<Button>(R.id.btnSaveTransaction)
        val cancelButton = findViewById<Button>(R.id.btnCancelTransaction)

        // The date defaults to today, in the same dd/MM/yyyy format as before
        dateInput.setText(
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        )

        // Tapping the date field or its calendar icon opens a date picker
        dateInput.setOnClickListener {
            showDatePicker(dateInput)
        }

        dateLayout.setEndIconOnClickListener {
            showDatePicker(dateInput)
        }

        // Expense is selected by default, so show the expense categories first
        categoryInput.setSimpleItems(expenseCategories)

        // When the type changes, show the matching categories and clear the old choice
        typeRadioGroup.setOnCheckedChangeListener { _, checkedId ->

            if (checkedId == R.id.rbExpense) {
                categoryInput.setSimpleItems(expenseCategories)
            } else {
                categoryInput.setSimpleItems(incomeCategories)
            }

            categoryInput.setText("", false)
        }

        // History sends the transaction ID when Edit is pressed.
        // If no ID was sent, the value is -1 and the screen works in add mode.
        val transactionId = intent.getIntExtra("transaction_id", -1)

        if (transactionId != -1) {

            titleText.text = "Edit transaction"
            findViewById<TextView>(R.id.tvAddTransactionSubtitle).text = "Update your transaction"
            saveButton.text = "Update transaction"

            lifecycleScope.launch(Dispatchers.IO) {

                val database = AppDatabase.getDatabase(applicationContext)

                val transaction =
                    database.transactionDao().getTransactionById(transactionId)

                withContext(Dispatchers.Main) {

                    if (transaction == null) {

                        Toast.makeText(
                            this@AddTransactionActivity,
                            "Transaction not found",
                            Toast.LENGTH_SHORT
                        ).show()

                        finish()

                    } else {

                        transactionToEdit = transaction

                        // Select the type first, because changing the type clears the category
                        if (transaction.type == "Income") {
                            incomeRadioButton.isChecked = true
                        } else {
                            expenseRadioButton.isChecked = true
                        }

                        amountInput.setText(
                            String.format(Locale.US, "%.2f", transaction.amount)
                        )
                        categoryInput.setText(transaction.category, false)
                        descriptionInput.setText(transaction.description)
                        dateInput.setText(transaction.date)
                    }
                }
            }
        }

        cancelButton.setOnClickListener {
            finish()
        }

        saveButton.setOnClickListener {

            // In edit mode, wait until the old transaction has finished loading
            if (transactionId != -1 && transactionToEdit == null) {
                return@setOnClickListener
            }

            val amountText = amountInput.text.toString().trim()
            val category = categoryInput.text.toString().trim()
            val description = descriptionInput.text.toString().trim()

            val type = if (expenseRadioButton.isChecked) {
                "Expense"
            } else {
                "Income"
            }

            if (amountText.isBlank() || category.isBlank()) {
                Toast.makeText(
                    this,
                    "Please enter amount and select a category",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull()

            if (amount == null || amount <= 0) {
                Toast.makeText(
                    this,
                    "Please enter a valid amount",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // The date chosen in the date field (already in dd/MM/yyyy format)
            val date = dateInput.text.toString()

            val transaction = Transaction(
                amount = amount,
                type = type,
                category = category,
                description = description,
                date = date
            )

            val oldTransaction = transactionToEdit

            lifecycleScope.launch(Dispatchers.IO) {

                val database = AppDatabase.getDatabase(applicationContext)

                if (oldTransaction == null) {

                    database.transactionDao().insert(transaction)

                } else {

                    // copy() keeps the same id,
                    // and replaces only the fields the user can edit
                    val updatedTransaction = oldTransaction.copy(
                        amount = amount,
                        type = type,
                        category = category,
                        description = description,
                        date = date
                    )

                    database.transactionDao().update(updatedTransaction)
                }

                withContext(Dispatchers.Main) {

                    val message = if (oldTransaction == null) {
                        "Transaction saved successfully!"
                    } else {
                        "Transaction updated successfully!"
                    }

                    Toast.makeText(
                        this@AddTransactionActivity,
                        message,
                        Toast.LENGTH_SHORT
                    ).show()

                    finish()
                }
            }
        }
    }

    private fun showDatePicker(dateInput: EditText) {

        val calendar = Calendar.getInstance()

        // Open the picker on the date already shown in the field (dd/MM/yyyy)
        val parts = dateInput.text.toString().split("/")

        if (parts.size == 3) {

            val day = parts[0].toIntOrNull()
            val month = parts[1].toIntOrNull()
            val year = parts[2].toIntOrNull()

            if (day != null && month != null && year != null) {
                // Calendar months start from 0 (January = 0), so subtract 1
                calendar.set(year, month - 1, day)
            }
        }

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->

                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay)

                // Save the chosen date in the same dd/MM/yyyy format as before
                dateInput.setText(
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(selectedCalendar.time)
                )
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // A transaction cannot happen in the future
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

        datePickerDialog.show()
    }
}
