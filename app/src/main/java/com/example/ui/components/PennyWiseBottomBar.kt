package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CharcoalMuted
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.viewmodel.AppScreen

sealed class NavItem(
    val screen: AppScreen,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    data object Dashboard : NavItem(
        AppScreen.DASHBOARD,
        "Home",
        Icons.Filled.Home,
        Icons.Outlined.Home,
        "nav_home"
    )

    data object Transactions : NavItem(
        AppScreen.TRANSACTIONS_LIST,
        "History",
        Icons.Filled.Receipt,
        Icons.Outlined.Receipt,
        "nav_history"
    )

    data object Add : NavItem(
        AppScreen.ADD_TRANSACTION,
        "Add",
        Icons.Filled.Add,
        Icons.Filled.Add,
        "nav_add"
    )

    data object Breakdown : NavItem(
        AppScreen.CATEGORY_BREAKDOWN,
        "Breakdown",
        Icons.Filled.PieChart,
        Icons.Outlined.PieChart,
        "nav_breakdown"
    )

    data object Budgets : NavItem(
        AppScreen.BUDGET_SETUP,
        "Budgets",
        Icons.Filled.Savings,
        Icons.Outlined.Savings,
        "nav_budgets"
    )
}

@Composable
fun PennyWiseBottomBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem.Dashboard,
        NavItem.Transactions,
        NavItem.Add,
        NavItem.Breakdown,
        NavItem.Budgets
    )

    NavigationBar(
        modifier = modifier,
        windowInsets = WindowInsets.navigationBars,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        items.forEach { item ->
            val isSelected = currentScreen == item.screen ||
                    (item == NavItem.Breakdown && currentScreen == AppScreen.MONTHLY_REPORT)

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GreenPrimary,
                    selectedTextColor = GreenPrimary,
                    indicatorColor = GreenLight,
                    unselectedIconColor = CharcoalMuted,
                    unselectedTextColor = CharcoalMuted
                ),
                modifier = Modifier.testTag(item.tag)
            )
        }
    }
}
