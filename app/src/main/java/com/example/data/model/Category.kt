package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class Category(
    val id: String,
    val name: String,
    val type: TransactionType,
    val icon: ImageVector,
    val color: Color
)

object Categories {
    val expenseCategories = listOf(
        Category(
            id = "food",
            name = "Food & Dining",
            type = TransactionType.EXPENSE,
            icon = Icons.Default.Restaurant,
            color = Color(0xFFF59E0B) // Amber
        ),
        Category(
            id = "shopping",
            name = "Shopping",
            type = TransactionType.EXPENSE,
            icon = Icons.Default.ShoppingCart,
            color = Color(0xFFEC4899) // Pink
        ),
        Category(
            id = "housing",
            name = "Housing & Rent",
            type = TransactionType.EXPENSE,
            icon = Icons.Default.Home,
            color = Color(0xFF6366F1) // Indigo
        ),
        Category(
            id = "transport",
            name = "Transportation",
            type = TransactionType.EXPENSE,
            icon = Icons.Default.DirectionsCar,
            color = Color(0xFF3B82F6) // Blue
        ),
        Category(
            id = "entertainment",
            name = "Entertainment",
            type = TransactionType.EXPENSE,
            icon = Icons.Default.Movie,
            color = Color(0xFF8B5CF6) // Purple
        ),
        Category(
            id = "health",
            name = "Healthcare",
            type = TransactionType.EXPENSE,
            icon = Icons.Default.LocalHospital,
            color = Color(0xFF10B981) // Emerald
        ),
        Category(
            id = "utilities",
            name = "Bills & Utilities",
            type = TransactionType.EXPENSE,
            icon = Icons.Default.FlashOn,
            color = Color(0xFF06B6D4) // Cyan
        ),
        Category(
            id = "other_expense",
            name = "Other Expense",
            type = TransactionType.EXPENSE,
            icon = Icons.Default.Category,
            color = Color(0xFF64748B) // Slate
        )
    )

    val incomeCategories = listOf(
        Category(
            id = "salary",
            name = "Salary & Wages",
            type = TransactionType.INCOME,
            icon = Icons.Default.Work,
            color = Color(0xFF059669) // Deep Green
        ),
        Category(
            id = "freelance",
            name = "Freelance / Side Gig",
            type = TransactionType.INCOME,
            icon = Icons.Default.Computer,
            color = Color(0xFF10B981) // Emerald
        ),
        Category(
            id = "investment",
            name = "Investments & Stocks",
            type = TransactionType.INCOME,
            icon = Icons.Default.TrendingUp,
            color = Color(0xFF0284C7) // Sky Blue
        ),
        Category(
            id = "gift",
            name = "Gifts & Bonus",
            type = TransactionType.INCOME,
            icon = Icons.Default.CardGiftcard,
            color = Color(0xFFF43F5E) // Coral
        ),
        Category(
            id = "other_income",
            name = "Other Income",
            type = TransactionType.INCOME,
            icon = Icons.Default.AccountBalance,
            color = Color(0xFF14B8A6) // Teal
        )
    )

    val allCategories = expenseCategories + incomeCategories

    fun getCategoryById(id: String): Category {
        return allCategories.find { it.id.equals(id, ignoreCase = true) }
            ?: Category(
                id = id,
                name = id.replaceFirstChar { it.uppercase() },
                type = TransactionType.EXPENSE,
                icon = Icons.Default.Paid,
                color = Color(0xFF64748B)
            )
    }
}
