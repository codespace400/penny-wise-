package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.ui.components.IncomeExpenseBarChart
import com.example.ui.components.SpendingLineChart
import com.example.ui.components.WeekComparisonData
import com.example.ui.theme.CharcoalMuted
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.CoralRed
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.viewmodel.CategorySpendItem
import com.example.viewmodel.DailySpendPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun MonthlyReportScreen(
    transactions: List<TransactionEntity>,
    monthOffset: Int,
    currencySymbol: String,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cal = Calendar.getInstance().apply { add(Calendar.MONTH, monthOffset) }
    val targetMonth = cal.get(Calendar.MONTH)
    val targetYear = cal.get(Calendar.YEAR)
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val monthTitle = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)

    // Filter transactions for this month
    val monthTxs = transactions.filter { tx ->
        val txCal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
        txCal.get(Calendar.MONTH) == targetMonth && txCal.get(Calendar.YEAR) == targetYear
    }

    val totalIncome = monthTxs.filter { it.transactionType == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpense = monthTxs.filter { it.transactionType == TransactionType.EXPENSE }.sumOf { it.amount }
    val netSavings = totalIncome - totalExpense
    val savingsRate = if (totalIncome > 0) ((netSavings / totalIncome) * 100).coerceIn(-100.0, 100.0) else 0.0

    // Build Daily Spending Trend points (1..daysInMonth)
    val dailyPoints = remember(monthTxs, daysInMonth) {
        val daysMap = (1..daysInMonth).associateWith { 0.0 }.toMutableMap()
        monthTxs.filter { it.transactionType == TransactionType.EXPENSE }.forEach { tx ->
            val day = Calendar.getInstance().apply { timeInMillis = tx.timestamp }.get(Calendar.DAY_OF_MONTH)
            daysMap[day] = (daysMap[day] ?: 0.0) + tx.amount
        }
        daysMap.map { (day, amount) ->
            DailySpendPoint(dayOfMonth = day, dayLabel = "$day", spendAmount = amount, incomeAmount = 0.0)
        }
    }

    // Build Weekly Income vs Expense Comparison Data (Week 1 to Week 4)
    val weeklyComparison = remember(monthTxs) {
        val weeks = listOf(
            Triple("W1 (1-7)", 1..7, "Week 1"),
            Triple("W2 (8-14)", 8..14, "Week 2"),
            Triple("W3 (15-21)", 15..21, "Week 3"),
            Triple("W4 (22+)", 22..31, "Week 4")
        )

        weeks.map { (label, range, _) ->
            val weekTxs = monthTxs.filter { tx ->
                val day = Calendar.getInstance().apply { timeInMillis = tx.timestamp }.get(Calendar.DAY_OF_MONTH)
                day in range
            }
            val inc = weekTxs.filter { it.transactionType == TransactionType.INCOME }.sumOf { it.amount }
            val exp = weekTxs.filter { it.transactionType == TransactionType.EXPENSE }.sumOf { it.amount }
            WeekComparisonData(label = label, income = inc, expense = exp)
        }
    }

    // Screen 5 Requirement: Top 5 spending categories
    val top5Categories = remember(monthTxs, totalExpense) {
        val expenseTxs = monthTxs.filter { it.transactionType == TransactionType.EXPENSE }
        val grouped = expenseTxs.groupBy { it.categoryId }
        grouped.map { (catId, txList) ->
            val catTotal = txList.sumOf { it.amount }
            val cat = Categories.getCategoryById(catId)
            val pct = if (totalExpense > 0) (catTotal / totalExpense).toFloat() else 0f
            CategorySpendItem(category = cat, totalAmount = catTotal, percentage = pct)
        }.sortedByDescending { it.totalAmount }.take(5)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Month Selector Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPrevMonth,
                    modifier = Modifier.testTag("report_prev_month")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Previous Month",
                        tint = CharcoalText,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = monthTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    )
                    Text(
                        text = "Monthly Financial Analytics",
                        style = MaterialTheme.typography.labelSmall,
                        color = CharcoalMuted
                    )
                }

                IconButton(
                    onClick = onNextMonth,
                    enabled = monthOffset < 0,
                    modifier = Modifier.testTag("report_next_month")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Next Month",
                        tint = if (monthOffset < 0) CharcoalText else CharcoalMuted.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Summary Metric Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                            text = "TOTAL INCOME",
                            style = MaterialTheme.typography.labelSmall,
                            color = CharcoalMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyFormatter.format(totalIncome, currencySymbol),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = GreenPrimary
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
                            text = "TOTAL EXPENSES",
                            style = MaterialTheme.typography.labelSmall,
                            color = CharcoalMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyFormatter.format(totalExpense, currencySymbol),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = CoralRed
                        )
                    }
                }
            }
        }

        // Net Savings & Rate Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "NET SAVINGS",
                            style = MaterialTheme.typography.labelSmall,
                            color = CharcoalMuted
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = CurrencyFormatter.formatSigned(netSavings, netSavings >= 0, currencySymbol),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = if (netSavings >= 0) GreenPrimary else CoralRed
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (savingsRate >= 0) GreenLight else CoralRed.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Savings: ${String.format("%.1f", savingsRate)}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (savingsRate >= 0) GreenPrimary else CoralRed,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Screen 5 Requirement: line chart of spending over the month
        item {
            SpendingLineChart(
                dailyPoints = dailyPoints,
                currencySymbol = currencySymbol
            )
        }

        // Screen 5 Requirement: income vs expense comparison bar chart
        item {
            IncomeExpenseBarChart(
                weeklyData = weeklyComparison,
                currencySymbol = currencySymbol
            )
        }

        // Screen 5 Requirement: top 5 spending categories
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Top 5 Spending Categories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (top5Categories.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No expenses recorded this month",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CharcoalMuted
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            top5Categories.forEachIndexed { index, item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Rank Badge
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "#${index + 1}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = CharcoalText,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(item.category.color.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = item.category.icon,
                                                contentDescription = item.category.name,
                                                tint = item.category.color,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(
                                                text = item.category.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = CharcoalText
                                            )
                                            Text(
                                                text = "${String.format("%.1f", item.percentage * 100)}% of month total",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = CharcoalMuted
                                            )
                                        }
                                    }

                                    Text(
                                        text = CurrencyFormatter.format(item.totalAmount, currencySymbol),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        ),
                                        color = CharcoalText
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(90.dp)) // padding for bottom bar
        }
    }
}
