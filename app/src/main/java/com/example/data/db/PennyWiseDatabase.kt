package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.BudgetEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
    entities = [TransactionEntity::class, BudgetEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PennyWiseDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: PennyWiseDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): PennyWiseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PennyWiseDatabase::class.java,
                    "pennywise_database"
                )
                    .addCallback(PennyWiseDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class PennyWiseDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.transactionDao(), database.budgetDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(transactionDao: TransactionDao, budgetDao: BudgetDao) {
            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance()

            fun timeAgo(daysAgo: Int, hour: Int = 12): Long {
                val c = Calendar.getInstance()
                c.timeInMillis = now
                c.add(Calendar.DAY_OF_YEAR, -daysAgo)
                c.set(Calendar.HOUR_OF_DAY, hour)
                c.set(Calendar.MINUTE, 30)
                return c.timeInMillis
            }

            val sampleBudgets = listOf(
                BudgetEntity(categoryId = "food", monthlyLimit = 550.0),
                BudgetEntity(categoryId = "shopping", monthlyLimit = 300.0),
                BudgetEntity(categoryId = "housing", monthlyLimit = 1200.0),
                BudgetEntity(categoryId = "transport", monthlyLimit = 220.0),
                BudgetEntity(categoryId = "entertainment", monthlyLimit = 180.0),
                BudgetEntity(categoryId = "utilities", monthlyLimit = 200.0),
                BudgetEntity(categoryId = "health", monthlyLimit = 150.0)
            )
            budgetDao.insertAll(sampleBudgets)

            val sampleTransactions = listOf(
                TransactionEntity(
                    title = "Monthly Salary",
                    amount = 4500.00,
                    type = "INCOME",
                    categoryId = "salary",
                    timestamp = timeAgo(13, 9),
                    note = "Techcorp payroll direct deposit"
                ),
                TransactionEntity(
                    title = "Apartment Rent",
                    amount = 1150.00,
                    type = "EXPENSE",
                    categoryId = "housing",
                    timestamp = timeAgo(12, 10),
                    note = "October rent payment"
                ),
                TransactionEntity(
                    title = "Whole Foods Market",
                    amount = 84.50,
                    type = "EXPENSE",
                    categoryId = "food",
                    timestamp = timeAgo(0, 14),
                    note = "Weekly organic groceries"
                ),
                TransactionEntity(
                    title = "Freelance UI Design",
                    amount = 750.00,
                    type = "INCOME",
                    categoryId = "freelance",
                    timestamp = timeAgo(1, 16),
                    note = "Client mobile app sprint"
                ),
                TransactionEntity(
                    title = "Uber Ride",
                    amount = 24.20,
                    type = "EXPENSE",
                    categoryId = "transport",
                    timestamp = timeAgo(1, 20),
                    note = "Trip to downtown meeting"
                ),
                TransactionEntity(
                    title = "Nike Store Shoes",
                    amount = 135.00,
                    type = "EXPENSE",
                    categoryId = "shopping",
                    timestamp = timeAgo(2, 15),
                    note = "Running sneakers"
                ),
                TransactionEntity(
                    title = "Blue Bottle Coffee",
                    amount = 14.75,
                    type = "EXPENSE",
                    categoryId = "food",
                    timestamp = timeAgo(2, 9),
                    note = "Breakfast latte & pastry"
                ),
                TransactionEntity(
                    title = "Electric & Water Bill",
                    amount = 118.40,
                    type = "EXPENSE",
                    categoryId = "utilities",
                    timestamp = timeAgo(4, 11),
                    note = "Monthly utility charges"
                ),
                TransactionEntity(
                    title = "Movie Night & IMAX",
                    amount = 38.00,
                    type = "EXPENSE",
                    categoryId = "entertainment",
                    timestamp = timeAgo(5, 19),
                    note = "2 Tickets + popcorn"
                ),
                TransactionEntity(
                    title = "Stock Dividend",
                    amount = 125.60,
                    type = "INCOME",
                    categoryId = "investment",
                    timestamp = timeAgo(6, 12),
                    note = "Quarterly index fund payout"
                ),
                TransactionEntity(
                    title = "Pharmacy & Vitamins",
                    amount = 42.50,
                    type = "EXPENSE",
                    categoryId = "health",
                    timestamp = timeAgo(7, 14),
                    note = "Wellness replenishment"
                ),
                TransactionEntity(
                    title = "Sushi Dinner with Friends",
                    amount = 68.20,
                    type = "EXPENSE",
                    categoryId = "food",
                    timestamp = timeAgo(8, 20),
                    note = "Dinner celebration"
                ),
                TransactionEntity(
                    title = "Metro Transit Pass",
                    amount = 65.00,
                    type = "EXPENSE",
                    categoryId = "transport",
                    timestamp = timeAgo(9, 8),
                    note = "Monthly train card reload"
                ),
                TransactionEntity(
                    title = "Tech Gadget & Cable",
                    amount = 49.99,
                    type = "EXPENSE",
                    categoryId = "shopping",
                    timestamp = timeAgo(10, 17),
                    note = "USB-C GaN fast charger"
                ),
                TransactionEntity(
                    title = "Netflix & Spotify",
                    amount = 26.98,
                    type = "EXPENSE",
                    categoryId = "entertainment",
                    timestamp = timeAgo(11, 10),
                    note = "Monthly subscriptions"
                )
            )
            transactionDao.insertAll(sampleTransactions)
        }
    }
}
