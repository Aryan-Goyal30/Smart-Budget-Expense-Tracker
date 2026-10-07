package com.example.smartbudgetexpensetracker

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TransactionDao {

    @Insert
    suspend fun insert(transaction: Transaction)

    @Update
    suspend fun update(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Int): Transaction?

    @Query("SELECT * FROM transactions ORDER BY id DESC")
    suspend fun getAllTransactions(): List<Transaction>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'Income'")
    suspend fun getTotalIncome(): Double

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'Expense'")
    suspend fun getTotalExpenses(): Double

    @Query("""
        SELECT COALESCE(SUM(amount), 0)
        FROM transactions
        WHERE type = 'Expense'
        AND date LIKE '%/' || :month || '/' || :year
    """)
    suspend fun getMonthlyExpenses(
        month: String,
        year: String
    ): Double

    @Query("""
        SELECT category, COALESCE(SUM(amount), 0) AS total
        FROM transactions
        WHERE type = 'Expense'
        GROUP BY category
        ORDER BY total DESC
    """)
    suspend fun getExpensesByCategory(): List<CategoryTotal>

    @Query("""
        SELECT date, COALESCE(SUM(amount), 0) AS total
        FROM transactions
        WHERE type = 'Expense'
        AND date LIKE '%/' || :month || '/' || :year
        GROUP BY date
        ORDER BY date
    """)
    suspend fun getDailyExpenses(
        month: String,
        year: String
    ): List<DailyExpenseTotal>

    @Query("""
        SELECT COALESCE(SUM(amount), 0)
        FROM transactions
        WHERE type = 'Income'
        AND date LIKE '%/' || :month || '/' || :year
    """)
    suspend fun getMonthlyIncome(
        month: String,
        year: String
    ): Double

    @Query("""
        SELECT category, COALESCE(SUM(amount), 0) AS total
        FROM transactions
        WHERE type = 'Expense'
        AND date LIKE '%/' || :month || '/' || :year
        GROUP BY category
        ORDER BY total DESC
    """)
    suspend fun getMonthlyExpensesByCategory(
        month: String,
        year: String
    ): List<CategoryTotal>

    @Delete
    suspend fun delete(transaction: Transaction)
}