package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.db.BudgetDao
import com.example.data.db.PennyWiseDatabase
import com.example.data.db.TransactionDao
import com.example.data.model.BudgetEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("pennywise_prefs", Context.MODE_PRIVATE)

    private val _currencySymbol = MutableStateFlow(prefs.getString("currency_symbol", "$") ?: "$")
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    private val _currencyCode = MutableStateFlow(prefs.getString("currency_code", "USD") ?: "USD")
    val currencyCode: StateFlow<String> = _currencyCode.asStateFlow()

    private val _dailyReminder = MutableStateFlow(prefs.getBoolean("daily_reminder", true))
    val dailyReminder: StateFlow<Boolean> = _dailyReminder.asStateFlow()

    private val _budgetAlerts = MutableStateFlow(prefs.getBoolean("budget_alerts", true))
    val budgetAlerts: StateFlow<Boolean> = _budgetAlerts.asStateFlow()

    private val _weeklySummary = MutableStateFlow(prefs.getBoolean("weekly_summary", true))
    val weeklySummary: StateFlow<Boolean> = _weeklySummary.asStateFlow()

    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allBudgets: Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()

    fun getTransactionsBetween(start: Long, end: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsBetween(start, end)

    fun getTransactionsByCategory(categoryId: String): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByCategory(categoryId)

    suspend fun addTransaction(transaction: TransactionEntity): Long =
        transactionDao.insertTransaction(transaction)

    suspend fun updateTransaction(transaction: TransactionEntity) =
        transactionDao.updateTransaction(transaction)

    suspend fun deleteTransaction(id: Long) =
        transactionDao.deleteById(id)

    suspend fun setBudget(categoryId: String, limit: Double) =
        budgetDao.insertOrUpdate(BudgetEntity(categoryId = categoryId, monthlyLimit = limit))

    suspend fun deleteBudget(categoryId: String) =
        budgetDao.deleteByCategoryId(categoryId)

    fun setCurrency(symbol: String, code: String) {
        prefs.edit().putString("currency_symbol", symbol).putString("currency_code", code).apply()
        _currencySymbol.value = symbol
        _currencyCode.value = code
    }

    fun setDailyReminder(enabled: Boolean) {
        prefs.edit().putBoolean("daily_reminder", enabled).apply()
        _dailyReminder.value = enabled
    }

    fun setBudgetAlerts(enabled: Boolean) {
        prefs.edit().putBoolean("budget_alerts", enabled).apply()
        _budgetAlerts.value = enabled
    }

    fun setWeeklySummary(enabled: Boolean) {
        prefs.edit().putBoolean("weekly_summary", enabled).apply()
        _weeklySummary.value = enabled
    }

    suspend fun resetSampleData() {
        transactionDao.clearAll()
        budgetDao.clearAll()
        PennyWiseDatabase.populateInitialData(transactionDao, budgetDao)
    }

    suspend fun clearAllData() {
        transactionDao.clearAll()
        budgetDao.clearAll()
    }

    fun generateCsvExport(transactions: List<TransactionEntity>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val sb = StringBuilder()
        sb.append("ID,Date,Title,Type,Category,Amount,Note\n")
        transactions.forEach { tx ->
            val formattedDate = dateFormat.format(Date(tx.timestamp))
            val cleanTitle = tx.title.replace(",", " ")
            val cleanNote = tx.note.replace(",", " ")
            sb.append("${tx.id},$formattedDate,\"$cleanTitle\",${tx.type},${tx.categoryId},${tx.amount},\"$cleanNote\"\n")
        }
        return sb.toString()
    }
}
