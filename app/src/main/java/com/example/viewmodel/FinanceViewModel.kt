package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.PennyWiseDatabase
import com.example.data.model.BudgetEntity
import com.example.data.model.Categories
import com.example.data.model.Category
import com.example.data.model.CategoryBudgetProgress
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AppScreen {
    DASHBOARD,
    ADD_TRANSACTION,
    TRANSACTIONS_LIST,
    CATEGORY_BREAKDOWN,
    MONTHLY_REPORT,
    BUDGET_SETUP,
    SETTINGS
}

enum class DateRangeFilter(val label: String) {
    THIS_MONTH("This Month"),
    LAST_30_DAYS("Last 30 Days"),
    THIS_YEAR("This Year"),
    ALL_TIME("All Time")
}

data class CategorySpendItem(
    val category: Category,
    val totalAmount: Double,
    val percentage: Float
)

data class DailySpendPoint(
    val dayOfMonth: Int,
    val dayLabel: String,
    val spendAmount: Double,
    val incomeAmount: Double
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val database = PennyWiseDatabase.getDatabase(application, viewModelScope)
    private val repository = FinanceRepository(
        transactionDao = database.transactionDao(),
        budgetDao = database.budgetDao(),
        context = application
    )

    // Current active screen navigation
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Navigation back stack for sub-screens
    private val screenHistory = mutableListOf<AppScreen>()

    // Currency & Settings
    val currencySymbol: StateFlow<String> = repository.currencySymbol
    val currencyCode: StateFlow<String> = repository.currencyCode
    val dailyReminder: StateFlow<Boolean> = repository.dailyReminder
    val budgetAlerts: StateFlow<Boolean> = repository.budgetAlerts
    val weeklySummary: StateFlow<Boolean> = repository.weeklySummary

    // All raw transactions & budgets
    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBudgets: StateFlow<List<BudgetEntity>> = repository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Month Selector for Monthly Report & Dashboard (0 = current, -1 = last month, etc.)
    private val _selectedMonthOffset = MutableStateFlow(0)
    val selectedMonthOffset: StateFlow<Int> = _selectedMonthOffset.asStateFlow()

    // Filter states for Transaction List screen
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterType = MutableStateFlow<TransactionType?>(null)
    val filterType: StateFlow<TransactionType?> = _filterType.asStateFlow()

    private val _filterCategoryId = MutableStateFlow<String?>(null)
    val filterCategoryId: StateFlow<String?> = _filterCategoryId.asStateFlow()

    private val _filterDateRange = MutableStateFlow(DateRangeFilter.ALL_TIME)
    val filterDateRange: StateFlow<DateRangeFilter> = _filterDateRange.asStateFlow()

    // Drill down category in Category Breakdown screen
    private val _drillDownCategory = MutableStateFlow<Category?>(null)
    val drillDownCategory: StateFlow<Category?> = _drillDownCategory.asStateFlow()

    // Add Transaction Form State
    private val _addType = MutableStateFlow(TransactionType.EXPENSE)
    val addType: StateFlow<TransactionType> = _addType.asStateFlow()

    private val _addAmountString = MutableStateFlow("0")
    val addAmountString: StateFlow<String> = _addAmountString.asStateFlow()

    private val _addSelectedCategory = MutableStateFlow<Category>(Categories.expenseCategories.first())
    val addSelectedCategory: StateFlow<Category> = _addSelectedCategory.asStateFlow()

    private val _addDateTimestamp = MutableStateFlow(System.currentTimeMillis())
    val addDateTimestamp: StateFlow<Long> = _addDateTimestamp.asStateFlow()

    private val _addNote = MutableStateFlow("")
    val addNote: StateFlow<String> = _addNote.asStateFlow()

    private val _addTitle = MutableStateFlow("")
    val addTitle: StateFlow<String> = _addTitle.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Editing budget dialog state
    private val _editingBudget = MutableStateFlow<CategoryBudgetProgress?>(null)
    val editingBudget: StateFlow<CategoryBudgetProgress?> = _editingBudget.asStateFlow()

    // Filtered transactions for the Transaction List Screen
    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        allTransactions,
        _searchQuery,
        _filterType,
        _filterCategoryId,
        _filterDateRange
    ) { txs, query, type, catId, dateRange ->
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()

        txs.filter { tx ->
            // Search query filter
            val matchesQuery = query.isBlank() ||
                    tx.title.contains(query, ignoreCase = true) ||
                    tx.note.contains(query, ignoreCase = true)

            // Type filter
            val matchesType = type == null || tx.transactionType == type

            // Category filter
            val matchesCat = catId == null || tx.categoryId.equals(catId, ignoreCase = true)

            // Date Range filter
            val matchesDate = when (dateRange) {
                DateRangeFilter.ALL_TIME -> true
                DateRangeFilter.THIS_MONTH -> {
                    val txCal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
                    cal.get(Calendar.YEAR) == txCal.get(Calendar.YEAR) &&
                            cal.get(Calendar.MONTH) == txCal.get(Calendar.MONTH)
                }
                DateRangeFilter.LAST_30_DAYS -> {
                    val thirtyDaysAgo = now - 30L * 24 * 60 * 60 * 1000
                    tx.timestamp in thirtyDaysAgo..now
                }
                DateRangeFilter.THIS_YEAR -> {
                    val txCal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
                    cal.get(Calendar.YEAR) == txCal.get(Calendar.YEAR)
                }
            }

            matchesQuery && matchesType && matchesCat && matchesDate
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Month name for report
    fun getFormattedMonth(offset: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, offset)
        return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }

    fun nextMonth() {
        if (_selectedMonthOffset.value < 0) {
            _selectedMonthOffset.value += 1
        }
    }

    fun prevMonth() {
        _selectedMonthOffset.value -= 1
    }

    // Navigation methods
    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            screenHistory.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (_drillDownCategory.value != null) {
            _drillDownCategory.value = null
            return true
        }
        if (screenHistory.isNotEmpty()) {
            _currentScreen.value = screenHistory.removeAt(screenHistory.lastIndex)
            return true
        }
        if (_currentScreen.value != AppScreen.DASHBOARD) {
            _currentScreen.value = AppScreen.DASHBOARD
            return true
        }
        return false
    }

    fun openCategoryDrillDown(category: Category) {
        _drillDownCategory.value = category
        navigateTo(AppScreen.CATEGORY_BREAKDOWN)
    }

    fun clearCategoryDrillDown() {
        _drillDownCategory.value = null
    }

    // Add Transaction Actions
    fun setAddType(type: TransactionType) {
        _addType.value = type
        val available = if (type == TransactionType.EXPENSE) {
            Categories.expenseCategories
        } else {
            Categories.incomeCategories
        }
        if (!available.contains(_addSelectedCategory.value)) {
            _addSelectedCategory.value = available.first()
        }
    }

    fun setAddCategory(category: Category) {
        _addSelectedCategory.value = category
    }

    fun setAddDate(timestamp: Long) {
        _addDateTimestamp.value = timestamp
    }

    fun setAddNote(note: String) {
        _addNote.value = note
    }

    fun setAddTitle(title: String) {
        _addTitle.value = title
    }

    fun onKeypadPress(key: String) {
        val current = _addAmountString.value
        when (key) {
            "DEL" -> {
                if (current.length > 1) {
                    _addAmountString.value = current.dropLast(1)
                } else {
                    _addAmountString.value = "0"
                }
            }
            "CLEAR" -> {
                _addAmountString.value = "0"
            }
            "." -> {
                if (!current.contains(".")) {
                    _addAmountString.value = "$current."
                }
            }
            else -> {
                // Number digit 0-9
                if (current == "0") {
                    _addAmountString.value = key
                } else {
                    // Limit to 2 decimal places
                    val parts = current.split(".")
                    if (parts.size > 1 && parts[1].length >= 2) {
                        return // already 2 decimal places
                    }
                    if (current.length < 10) {
                        _addAmountString.value = current + key
                    }
                }
            }
        }
    }

    fun saveTransaction() {
        val amount = _addAmountString.value.toDoubleOrNull() ?: 0.0
        if (amount <= 0.0) {
            _toastMessage.value = "Please enter a valid amount greater than 0"
            return
        }

        val title = if (_addTitle.value.isNotBlank()) {
            _addTitle.value.trim()
        } else {
            _addSelectedCategory.value.name
        }

        val transaction = TransactionEntity(
            title = title,
            amount = amount,
            type = _addType.value.name,
            categoryId = _addSelectedCategory.value.id,
            timestamp = _addDateTimestamp.value,
            note = _addNote.value.trim()
        )

        viewModelScope.launch {
            repository.addTransaction(transaction)
            // Reset input form
            _addAmountString.value = "0"
            _addNote.value = ""
            _addTitle.value = ""
            _addDateTimestamp.value = System.currentTimeMillis()
            _toastMessage.value = "Transaction saved successfully!"
            navigateTo(AppScreen.DASHBOARD)
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
            _toastMessage.value = "Transaction deleted"
        }
    }

    // Filter controls for Transaction List
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterType(type: TransactionType?) {
        _filterType.value = type
    }

    fun setFilterCategoryId(catId: String?) {
        _filterCategoryId.value = catId
    }

    fun setFilterDateRange(range: DateRangeFilter) {
        _filterDateRange.value = range
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _filterType.value = null
        _filterCategoryId.value = null
        _filterDateRange.value = DateRangeFilter.ALL_TIME
    }

    // Budget setup actions
    fun openEditBudget(budgetProgress: CategoryBudgetProgress) {
        _editingBudget.value = budgetProgress
    }

    fun closeEditBudget() {
        _editingBudget.value = null
    }

    fun saveBudgetLimit(categoryId: String, newLimit: Double) {
        viewModelScope.launch {
            repository.setBudget(categoryId, newLimit)
            _editingBudget.value = null
            _toastMessage.value = "Budget updated"
        }
    }

    // Settings actions
    fun setCurrency(symbol: String, code: String) {
        repository.setCurrency(symbol, code)
        _toastMessage.value = "Currency updated to $code ($symbol)"
    }

    fun setDailyReminder(enabled: Boolean) = repository.setDailyReminder(enabled)
    fun setBudgetAlerts(enabled: Boolean) = repository.setBudgetAlerts(enabled)
    fun setWeeklySummary(enabled: Boolean) = repository.setWeeklySummary(enabled)

    fun resetSampleData() {
        viewModelScope.launch {
            repository.resetSampleData()
            _toastMessage.value = "Sample data refreshed"
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _toastMessage.value = "All data cleared"
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun getCsvExportData(): String {
        return repository.generateCsvExport(allTransactions.value)
    }
}
