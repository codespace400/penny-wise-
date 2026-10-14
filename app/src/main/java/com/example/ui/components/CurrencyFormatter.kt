package com.example.ui.components

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyFormatter {
    private val formatter = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))

    fun format(amount: Double, symbol: String = "$"): String {
        return "$symbol${formatter.format(amount)}"
    }

    fun formatSigned(amount: Double, isIncome: Boolean, symbol: String = "$"): String {
        val prefix = if (isIncome) "+$symbol" else "-$symbol"
        return "$prefix${formatter.format(amount)}"
    }

    fun formatCompact(amount: Double, symbol: String = "$"): String {
        return when {
            amount >= 1_000_000 -> "$symbol${String.format(Locale.US, "%.1fM", amount / 1_000_000)}"
            amount >= 1_000 -> "$symbol${String.format(Locale.US, "%.1fK", amount / 1_000)}"
            else -> "$symbol${formatter.format(amount)}"
        }
    }
}
