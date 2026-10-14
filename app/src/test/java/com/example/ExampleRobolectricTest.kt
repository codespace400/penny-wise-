package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.components.CurrencyFormatter
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("PennyWise", appName)
  }

  @Test
  fun `currency formatter formats correctly`() {
    val formatted = CurrencyFormatter.format(12450.80, "$")
    assertEquals("$12,450.80", formatted)

    val signedIncome = CurrencyFormatter.formatSigned(500.0, true, "$")
    assertEquals("+$500.00", signedIncome)

    val signedExpense = CurrencyFormatter.formatSigned(84.50, false, "$")
    assertEquals("-$84.50", signedExpense)
  }
}
