package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Categories
import com.example.data.model.Category
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.components.CurrencyFormatter
import com.example.ui.components.HorizontalCategoryBarItem
import com.example.ui.components.TransactionItemCard
import com.example.ui.theme.CharcoalMuted
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.CoralRed
import com.example.ui.theme.GreenPrimary
import com.example.viewmodel.CategorySpendItem

@Composable
fun CategoryBreakdownScreen(
    transactions: List<TransactionEntity>,
    drillDownCategory: Category?,
    currencySymbol: String,
    onCategoryClick: (Category) -> Unit,
    onClearDrillDown: () -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (drillDownCategory != null) {
        BackHandler { onClearDrillDown() }
    }

    val expenseTxs = transactions.filter { it.transactionType == TransactionType.EXPENSE }
    val totalExpense = expenseTxs.sumOf { it.amount }

    // Group expenses by category
    val spendItems = Categories.expenseCategories.map { category ->
        val catTxs = expenseTxs.filter { it.categoryId.equals(category.id, ignoreCase = true) }
        val catTotal = catTxs.sumOf { it.amount }
        val percentage = if (totalExpense > 0) (catTotal / totalExpense).toFloat() else 0f
        CategorySpendItem(category = category, totalAmount = catTotal, percentage = percentage)
    }.filter { it.totalAmount > 0 }.sortedByDescending { it.totalAmount }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (drillDownCategory != null) {
            // Screen 4 Requirement: Drill into category transactions view
            val categoryTxs = expenseTxs.filter { it.categoryId.equals(drillDownCategory.id, ignoreCase = true) }
            val catTotal = categoryTxs.sumOf { it.amount }
            val catAvg = if (categoryTxs.isNotEmpty()) catTotal / categoryTxs.size else 0.0

            // Drill down header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClearDrillDown,
                    modifier = Modifier.testTag("drilldown_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to categories",
                        tint = CharcoalText
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(drillDownCategory.color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = drillDownCategory.icon,
                        contentDescription = drillDownCategory.name,
                        tint = drillDownCategory.color,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = drillDownCategory.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    )
                    Text(
                        text = "${categoryTxs.size} transactions recorded",
                        style = MaterialTheme.typography.labelSmall,
                        color = CharcoalMuted
                    )
                }
            }

            // Stat Cards for Category
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "TOTAL SPENT",
                            style = MaterialTheme.typography.labelSmall,
                            color = CharcoalMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyFormatter.format(catTotal, currencySymbol),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            ),
                            color = drillDownCategory.color
                        )
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "AVERAGE TRANSACTION",
                            style = MaterialTheme.typography.labelSmall,
                            color = CharcoalMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyFormatter.format(catAvg, currencySymbol),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            ),
                            color = CharcoalText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Transaction list for this category
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Transactions History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                if (categoryTxs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No transactions in this category",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CharcoalMuted
                            )
                        }
                    }
                } else {
                    items(categoryTxs, key = { it.id }) { tx ->
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
        } else {
            // Screen 4 Requirement: Category Breakdown Horizontal bar chart overview
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
                        text = "Category Breakdown",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "${spendItems.size} Categories",
                            style = MaterialTheme.typography.labelSmall,
                            color = CharcoalMuted,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Tap any category to view all corresponding transactions",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CharcoalMuted
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Total Expense Banner
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Spending",
                                style = MaterialTheme.typography.labelSmall,
                                color = CharcoalMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = CurrencyFormatter.format(totalExpense, currencySymbol),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = CoralRed
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = CharcoalText,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Screen 4 Requirement: Horizontal bar chart per category
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (spendItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No spending recorded yet.\nAdd expenses to see category breakdown!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CharcoalMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(spendItems, key = { it.category.id }) { item ->
                        HorizontalCategoryBarItem(
                            item = item,
                            currencySymbol = currencySymbol,
                            onCategoryClick = onCategoryClick
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
