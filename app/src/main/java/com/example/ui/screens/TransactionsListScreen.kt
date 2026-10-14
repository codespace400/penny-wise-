package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Categories
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.components.CurrencyFormatter
import com.example.ui.components.TransactionItemCard
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalMuted
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.CoralRed
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.viewmodel.DateRangeFilter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsListScreen(
    transactions: List<TransactionEntity>,
    searchQuery: String,
    selectedType: TransactionType?,
    selectedCategoryId: String?,
    selectedDateRange: DateRangeFilter,
    currencySymbol: String,
    onSearchChange: (String) -> Unit,
    onTypeChange: (TransactionType?) -> Unit,
    onCategoryChange: (String?) -> Unit,
    onDateRangeChange: (DateRangeFilter) -> Unit,
    onClearFilters: () -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    // Group transactions by Date Header (e.g., "Today", "Yesterday", "October 14, 2026")
    val groupedTransactions = remember(transactions) {
        val now = Calendar.getInstance()
        val todayYear = now.get(Calendar.YEAR)
        val todayDay = now.get(Calendar.DAY_OF_YEAR)

        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val yesterdayYear = yesterday.get(Calendar.YEAR)
        val yesterdayDay = yesterday.get(Calendar.DAY_OF_YEAR)

        val fullDateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())

        transactions.groupBy { tx ->
            val txCal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
            val txYear = txCal.get(Calendar.YEAR)
            val txDay = txCal.get(Calendar.DAY_OF_YEAR)

            when {
                txYear == todayYear && txDay == todayDay -> "Today"
                txYear == yesterdayYear && txDay == yesterdayDay -> "Yesterday"
                else -> fullDateFormat.format(Date(tx.timestamp))
            }
        }
    }

    val totalIncome = transactions.filter { it.transactionType == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.transactionType == TransactionType.EXPENSE }.sumOf { it.amount }
    val netSum = totalIncome - totalExpense

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Screen Title & Summary Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transactions",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalText
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "${transactions.size} records",
                        style = MaterialTheme.typography.labelSmall,
                        color = CharcoalMuted,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Screen 3 Requirement: Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search title, note, or merchant...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = CharcoalMuted
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = CharcoalMuted
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transaction_search_bar")
            )
        }

        // Screen 3 Requirement: Filter chips (type, date range, category)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Type Filters: All, Expense, Income
            item {
                FilterChip(
                    selected = selectedType == null,
                    onClick = { onTypeChange(null) },
                    label = { Text("All Types") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenLight,
                        selectedLabelColor = GreenPrimary
                    ),
                    modifier = Modifier.testTag("filter_type_all")
                )
            }
            item {
                FilterChip(
                    selected = selectedType == TransactionType.EXPENSE,
                    onClick = {
                        onTypeChange(if (selectedType == TransactionType.EXPENSE) null else TransactionType.EXPENSE)
                    },
                    label = { Text("Expenses") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CoralRed.copy(alpha = 0.15f),
                        selectedLabelColor = CoralRed
                    ),
                    modifier = Modifier.testTag("filter_type_expense")
                )
            }
            item {
                FilterChip(
                    selected = selectedType == TransactionType.INCOME,
                    onClick = {
                        onTypeChange(if (selectedType == TransactionType.INCOME) null else TransactionType.INCOME)
                    },
                    label = { Text("Income") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenLight,
                        selectedLabelColor = GreenPrimary
                    ),
                    modifier = Modifier.testTag("filter_type_income")
                )
            }

            // Date Range Filters
            DateRangeFilter.entries.forEach { range ->
                item {
                    FilterChip(
                        selected = selectedDateRange == range,
                        onClick = { onDateRangeChange(range) },
                        label = { Text(range.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("filter_date_${range.name}")
                    )
                }
            }

            // Category Filter Dropdown
            item {
                Box {
                    FilterChip(
                        selected = selectedCategoryId != null,
                        onClick = { categoryDropdownExpanded = true },
                        label = {
                            val catName = selectedCategoryId?.let { Categories.getCategoryById(it).name } ?: "Category"
                            Text(catName)
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter by category",
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("filter_category_button")
                    )

                    DropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Categories") },
                            onClick = {
                                onCategoryChange(null)
                                categoryDropdownExpanded = false
                            }
                        )
                        Categories.allCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = cat.icon,
                                        contentDescription = cat.name,
                                        tint = cat.color,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    onCategoryChange(cat.id)
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Net summary pill for active filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Net: ${CurrencyFormatter.formatSigned(netSum, netSum >= 0, currencySymbol)}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (netSum >= 0) GreenPrimary else CoralRed
            )

            if (searchQuery.isNotEmpty() || selectedType != null || selectedCategoryId != null || selectedDateRange != DateRangeFilter.ALL_TIME) {
                Text(
                    text = "Reset Filters",
                    style = MaterialTheme.typography.labelSmall,
                    color = GreenPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable { onClearFilters() }
                        .padding(4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Screen 3 Requirement: Full scrollable list grouped by date
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No transactions found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try adjusting your search or filters",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CharcoalMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                groupedTransactions.forEach { (dateHeader, txList) ->
                    item {
                        Text(
                            text = dateHeader,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalMuted,
                            modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
                        )
                    }

                    items(txList, key = { it.id }) { tx ->
                        TransactionItemCard(
                            transaction = tx,
                            currencySymbol = currencySymbol,
                            onDelete = { onDeleteTransaction(tx.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(90.dp)) // padding for bottom bar
                }
            }
        }
    }
}
