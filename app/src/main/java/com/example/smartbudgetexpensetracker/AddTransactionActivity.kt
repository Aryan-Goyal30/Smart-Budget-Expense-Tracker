package com.example.smartbudgetexpensetracker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddTransactionActivity : AppCompatActivity() {

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

        val amountInput = findViewById<EditText>(R.id.etAmount)
        val categoryInput = findViewById<EditText>(R.id.etCategory)
        val descriptionInput = findViewById<EditText>(R.id.etDescription)

        val expenseRadioButton = findViewById<RadioButton>(R.id.rbExpense)
        val saveButton = findViewById<Button>(R.id.btnSaveTransaction)
        val cancelButton = findViewById<Button>(R.id.btnCancelTransaction)

        cancelButton.setOnClickListener {
            finish()
        }

        saveButton.setOnClickListener {

            val amountText = amountInput.text.toString().trim()
            val category = categoryInput.text.toString()
                .trim()
                .lowercase()
                .replaceFirstChar { it.uppercase() }
            val description = descriptionInput.text.toString().trim()

            val type = if (expenseRadioButton.isChecked) {
                "Expense"
            } else {
                "Income"
            }

            if (amountText.isBlank() || category.isBlank()) {
                Toast.makeText(
                    this,
                    "Please enter amount and category",
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

            val date = SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
            ).format(Date())

            val transaction = Transaction(
                amount = amount,
                type = type,
                category = category,
                description = description,
                date = date
            )

            lifecycleScope.launch(Dispatchers.IO) {

                val database = AppDatabase.getDatabase(applicationContext)

                database.transactionDao().insert(transaction)

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@AddTransactionActivity,
                        "Transaction saved successfully!",
                        Toast.LENGTH_SHORT
                    ).show()

                    finish()
                }
            }
        }
    }
}