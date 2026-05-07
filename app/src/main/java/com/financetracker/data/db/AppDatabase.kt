package com.financetracker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.financetracker.data.db.dao.CategoryDao
import com.financetracker.data.db.dao.ParsingRuleDao
import com.financetracker.data.db.dao.PaymentAccountDao
import com.financetracker.data.db.dao.TransactionDao
import com.financetracker.data.db.entity.CategoryEntity
import com.financetracker.data.db.entity.ParsingRuleEntity
import com.financetracker.data.db.entity.PaymentAccountEntity
import com.financetracker.data.db.entity.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        PaymentAccountEntity::class,
        ParsingRuleEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : androidx.room.RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun paymentAccountDao(): PaymentAccountDao
    abstract fun parsingRuleDao(): ParsingRuleDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE payment_accounts ADD COLUMN color TEXT NOT NULL DEFAULT '#4CAF50'")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE payment_accounts ADD COLUMN balance REAL NOT NULL DEFAULT 0.0")
            }
        }

        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "finance_tracker.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .addCallback(SeedCallback())
                .build()
        }
    }

    private class SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                // Categories and accounts are seeded via the repository layer
                // because we need the DAO instances, which require built database
            }
        }
    }
}
