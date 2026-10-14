package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey
    val categoryId: String,
    val monthlyLimit: Double
)

data class CategoryBudgetProgress(
    val category: Category,
    val monthlyLimit: Double,
    val spentAmount: Double
) {
    val remainingAmount: Double
        get() = monthlyLimit - spentAmount

    val progressPercentage: Float
        get() = if (monthlyLimit > 0) {
            (spentAmount / monthlyLimit).toFloat().coerceIn(0f, 2f)
        } else 0f

    val isOverBudget: Boolean
        get() = spentAmount > monthlyLimit

    val isNearBudget: Boolean
        get() = !isOverBudget && progressPercentage >= 0.80f
}
