package com.example.smartbudgetexpensetracker

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val amount: Double,
    val type: String,
    val category: String,
    val description: String,
    val date: String
)