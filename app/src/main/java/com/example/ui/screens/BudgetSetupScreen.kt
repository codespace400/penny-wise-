package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BudgetEntity
import com.example.data.model.Categories
import com.example.data.model.Category
import com.example.data.model.CategoryBudgetProgress
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.components.CurrencyFormatter
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalMuted
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.CoralLight
import com.example.ui.theme.CoralRed
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import java.util.Calendar

@Composable
fun BudgetSetupScreen(
    transactions: List<TransactionEntity>,
    budgets: List<BudgetEntity>,
    currencySymbol: String,
    onSaveBudget: (String, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val cal = Calendar.getInstance()
    val currentMonth = cal.get(Calendar.MONTH)
    val currentYear = cal.get(Calendar.YEAR)

    // Current month's expenses
    val thisMonthExpenses = transactions.filter { tx ->
        val txCal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
        tx.transactionType == TransactionType.EXPENSE &&
                txCal.get(Calendar.MONTH) == currentMonth &&
                txCal.get(Calendar.YEAR) == currentYear
    }

    // Build budget progress list for all expense categories
    val budgetProgressList = Categories.expenseCategories.map { category ->
        val existingBudget = budgets.find { it.categoryId.equals(category.id, ignoreCase = true) }
        val limit = existingBudget?.monthlyLimit ?: 0.0
        val spent = thisMonthExpenses
            .filter { it.categoryId.equals(category.id, ignoreCase = true) }
            .sumOf { it.amount }
        CategoryBudgetProgress(
            category = category,
            monthlyLimit = limit,
            spentAmount = spent
        )
    }

    val totalBudget = budgets.sumOf { it.monthlyLimit }
    val totalSpentOnBudgeted = budgetProgressList.filter { it.monthlyLimit > 0 }.sumOf { it.spentAmount }
    val overallOverBudget = totalSpentOnBudgeted > totalBudget && totalBudget > 0
    val overBudgetCount = budgetProgressList.count { it.monthlyLimit > 0 && it.isOverBudget }

    // Dialog state for setting or editing limit
    var selectedCategoryForEdit by remember { mutableStateOf<CategoryBudgetProgress?>(null) }
    var inputLimitString by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
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
                    text = "Budget Setup",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalText
                )

                if (overBudgetCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CoralLight
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = CoralRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$overBudgetCount Over Budget",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CoralRed
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Control your monthly spending limits and prevent overspending",
                style = MaterialTheme.typography.bodyMedium,
                color = CharcoalMuted
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Screen 6 Requirement: Overall monthly budget summary
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL MONTHLY BUDGET",
                                style = MaterialTheme.typography.labelSmall,
                                color = CharcoalMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = CurrencyFormatter.format(totalBudget, currencySymbol),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = CharcoalText
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (overallOverBudget) CoralLight else GreenLight
                        ) {
                            Text(
                                text = if (overallOverBudget) "Over Limit" else "On Track",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (overallOverBudget) CoralRed else GreenPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val overallProgress = if (totalBudget > 0) {
                        (totalSpentOnBudgeted / totalBudget).toFloat().coerceIn(0f, 1f)
                    } else 0f

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(overallProgress)
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(if (overallOverBudget) CoralRed else GreenPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Spent: ${CurrencyFormatter.format(totalSpentOnBudgeted, currencySymbol)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CharcoalMuted
                        )
                        Text(
                            text = "Remaining: ${CurrencyFormatter.format(maxOf(0.0, totalBudget - totalSpentOnBudgeted), currencySymbol)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (overallOverBudget) CoralRed else CharcoalText
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Screen 6 Requirement: set monthly limit per category, progress bars showing spent vs limit, red warning when over budget
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(budgetProgressList, key = { it.category.id }) { item ->
                val hasLimit = item.monthlyLimit > 0
                val progress = if (hasLimit) (item.spentAmount / item.monthlyLimit).toFloat().coerceIn(0f, 1f) else 0f
                val isOver = hasLimit && item.isOverBudget
                val isNear = hasLimit && item.isNearBudget

                val barColor = when {
                    isOver -> CoralRed
                    isNear -> AccentAmber
                    else -> GreenPrimary
                }

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = if (isOver) androidx.compose.foundation.BorderStroke(1.5.dp, CoralRed) else null,
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("budget_item_${item.category.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(item.category.color.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.category.icon,
                                        contentDescription = item.category.name,
                                        tint = item.category.color,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = item.category.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalText
                                    )
                                    Text(
                                        text = if (hasLimit) {
                                            "Limit: ${CurrencyFormatter.format(item.monthlyLimit, currencySymbol)}"
                                        } else {
                                            "No budget set"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CharcoalMuted
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    selectedCategoryForEdit = item
                                    inputLimitString = if (item.monthlyLimit > 0) item.monthlyLimit.toInt().toString() else "200"
                                },
                                modifier = Modifier.testTag("edit_budget_${item.category.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit budget limit",
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            if (hasLimit) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progress)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(barColor)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${CurrencyFormatter.format(item.spentAmount, currencySymbol)} spent",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = if (isOver) CoralRed else CharcoalText
                            )

                            if (hasLimit) {
                                val pct = (item.spentAmount / item.monthlyLimit * 100).toInt()
                                Text(
                                    text = "$pct%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = barColor
                                )
                            } else {
                                Text(
                                    text = "Tap edit to set limit",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CharcoalMuted
                                )
                            }
                        }

                        // Red Warning Banner when Over Budget
                        if (isOver) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = CoralLight,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Over Budget Warning",
                                        tint = CoralRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Over budget by ${CurrencyFormatter.format(item.spentAmount - item.monthlyLimit, currencySymbol)}!",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = CoralRed
                                    )
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

    // Edit Budget Limit Dialog
    selectedCategoryForEdit?.let { editItem ->
        AlertDialog(
            onDismissRequest = { selectedCategoryForEdit = null },
            title = {
                Text(
                    text = "Set Monthly Limit",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Category: ${editItem.category.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CharcoalMuted
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = inputLimitString,
                        onValueChange = { inputLimitString = it },
                        label = { Text("Monthly Limit ($currencySymbol)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("budget_limit_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick increment pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(50, 100, 200, 500).forEach { inc ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        val current = inputLimitString.toDoubleOrNull() ?: 0.0
                                        inputLimitString = (current + inc).toInt().toString()
                                    }
                            ) {
                                Text(
                                    text = "+$inc",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalText,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newLimit = inputLimitString.toDoubleOrNull() ?: 0.0
                        onSaveBudget(editItem.category.id, newLimit)
                        selectedCategoryForEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    modifier = Modifier.testTag("save_budget_limit_button")
                ) {
                    Text("Save Limit")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { selectedCategoryForEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
