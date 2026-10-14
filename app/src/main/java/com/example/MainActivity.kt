package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.PennyWiseBottomBar
import com.example.ui.screens.AddTransactionScreen
import com.example.ui.screens.BudgetSetupScreen
import com.example.ui.screens.CategoryBreakdownScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MonthlyReportScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TransactionsListScreen
import com.example.ui.theme.CharcoalMuted
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.PennyWiseTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.FinanceViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PennyWiseTheme {
                PennyWiseApp()
            }
        }
    }
}

@Composable
fun PennyWiseApp(viewModel: FinanceViewModel = viewModel()) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val allBudgets by viewModel.allBudgets.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
    val currencyCode by viewModel.currencyCode.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            PennyWiseBottomBar(
                currentScreen = currentScreen,
                onNavigate = { screen -> viewModel.navigateTo(screen) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
            ) {
                when (currentScreen) {
                    AppScreen.DASHBOARD -> {
                        DashboardScreen(
                            transactions = allTransactions,
                            currencySymbol = currencySymbol,
                            onNavigate = { screen -> viewModel.navigateTo(screen) },
                            onCategoryClick = { category -> viewModel.openCategoryDrillDown(category) },
                            onDeleteTransaction = { id -> viewModel.deleteTransaction(id) }
                        )
                    }

                    AppScreen.ADD_TRANSACTION -> {
                        val currentType by viewModel.addType.collectAsStateWithLifecycle()
                        val amountString by viewModel.addAmountString.collectAsStateWithLifecycle()
                        val selectedCategory by viewModel.addSelectedCategory.collectAsStateWithLifecycle()
                        val dateTimestamp by viewModel.addDateTimestamp.collectAsStateWithLifecycle()
                        val note by viewModel.addNote.collectAsStateWithLifecycle()
                        val title by viewModel.addTitle.collectAsStateWithLifecycle()

                        AddTransactionScreen(
                            currentType = currentType,
                            amountString = amountString,
                            selectedCategory = selectedCategory,
                            timestamp = dateTimestamp,
                            note = note,
                            title = title,
                            currencySymbol = currencySymbol,
                            onTypeChange = { viewModel.setAddType(it) },
                            onCategoryChange = { viewModel.setAddCategory(it) },
                            onDateChange = { viewModel.setAddDate(it) },
                            onNoteChange = { viewModel.setAddNote(it) },
                            onTitleChange = { viewModel.setAddTitle(it) },
                            onKeypadPress = { viewModel.onKeypadPress(it) },
                            onSave = { viewModel.saveTransaction() },
                            onBack = { viewModel.navigateBack() }
                        )
                    }

                    AppScreen.TRANSACTIONS_LIST -> {
                        val filteredList by viewModel.filteredTransactions.collectAsStateWithLifecycle()
                        val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
                        val filterType by viewModel.filterType.collectAsStateWithLifecycle()
                        val filterCategoryId by viewModel.filterCategoryId.collectAsStateWithLifecycle()
                        val filterDateRange by viewModel.filterDateRange.collectAsStateWithLifecycle()

                        TransactionsListScreen(
                            transactions = filteredList,
                            searchQuery = searchQuery,
                            selectedType = filterType,
                            selectedCategoryId = filterCategoryId,
                            selectedDateRange = filterDateRange,
                            currencySymbol = currencySymbol,
                            onSearchChange = { viewModel.setSearchQuery(it) },
                            onTypeChange = { viewModel.setFilterType(it) },
                            onCategoryChange = { viewModel.setFilterCategoryId(it) },
                            onDateRangeChange = { viewModel.setFilterDateRange(it) },
                            onClearFilters = { viewModel.clearFilters() },
                            onDeleteTransaction = { id -> viewModel.deleteTransaction(id) }
                        )
                    }

                    AppScreen.CATEGORY_BREAKDOWN, AppScreen.MONTHLY_REPORT -> {
                        // Analytics Container with top tab switch between Category Breakdown & Monthly Report
                        Column(modifier = Modifier.fillMaxSize()) {
                            val drillDownCategory by viewModel.drillDownCategory.collectAsStateWithLifecycle()

                            if (drillDownCategory == null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(4.dp)
                                ) {
                                    val isBreakdown = currentScreen == AppScreen.CATEGORY_BREAKDOWN
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isBreakdown) MaterialTheme.colorScheme.surface else Color.Transparent,
                                        shadowElevation = if (isBreakdown) 1.dp else 0.dp,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { viewModel.navigateTo(AppScreen.CATEGORY_BREAKDOWN) }
                                    ) {
                                        Text(
                                            text = "Category Breakdown",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isBreakdown) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isBreakdown) CharcoalText else CharcoalMuted,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }

                                    val isReport = currentScreen == AppScreen.MONTHLY_REPORT
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isReport) MaterialTheme.colorScheme.surface else Color.Transparent,
                                        shadowElevation = if (isReport) 1.dp else 0.dp,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { viewModel.navigateTo(AppScreen.MONTHLY_REPORT) }
                                    ) {
                                        Text(
                                            text = "Monthly Report",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isReport) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isReport) CharcoalText else CharcoalMuted,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                }
                            }

                            if (currentScreen == AppScreen.CATEGORY_BREAKDOWN) {
                                CategoryBreakdownScreen(
                                    transactions = allTransactions,
                                    drillDownCategory = drillDownCategory,
                                    currencySymbol = currencySymbol,
                                    onCategoryClick = { category -> viewModel.openCategoryDrillDown(category) },
                                    onClearDrillDown = { viewModel.clearCategoryDrillDown() },
                                    onDeleteTransaction = { id -> viewModel.deleteTransaction(id) }
                                )
                            } else {
                                val monthOffset by viewModel.selectedMonthOffset.collectAsStateWithLifecycle()
                                MonthlyReportScreen(
                                    transactions = allTransactions,
                                    monthOffset = monthOffset,
                                    currencySymbol = currencySymbol,
                                    onPrevMonth = { viewModel.prevMonth() },
                                    onNextMonth = { viewModel.nextMonth() }
                                )
                            }
                        }
                    }

                    AppScreen.BUDGET_SETUP -> {
                        BudgetSetupScreen(
                            transactions = allTransactions,
                            budgets = allBudgets,
                            currencySymbol = currencySymbol,
                            onSaveBudget = { catId, limit -> viewModel.saveBudgetLimit(catId, limit) }
                        )
                    }

                    AppScreen.SETTINGS -> {
                        val dailyReminder by viewModel.dailyReminder.collectAsStateWithLifecycle()
                        val budgetAlerts by viewModel.budgetAlerts.collectAsStateWithLifecycle()
                        val weeklySummary by viewModel.weeklySummary.collectAsStateWithLifecycle()

                        SettingsScreen(
                            currentSymbol = currencySymbol,
                            currentCode = currencyCode,
                            dailyReminder = dailyReminder,
                            budgetAlerts = budgetAlerts,
                            weeklySummary = weeklySummary,
                            onCurrencyChange = { symbol, code -> viewModel.setCurrency(symbol, code) },
                            onDailyReminderChange = { viewModel.setDailyReminder(it) },
                            onBudgetAlertsChange = { viewModel.setBudgetAlerts(it) },
                            onWeeklySummaryChange = { viewModel.setWeeklySummary(it) },
                            onResetSampleData = { viewModel.resetSampleData() },
                            onClearAllData = { viewModel.clearAllData() },
                            getCsvData = { viewModel.getCsvExportData() },
                            onBack = { viewModel.navigateBack() }
                        )
                    }
                }
            }
        }
    }
}
