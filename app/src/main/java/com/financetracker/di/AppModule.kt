package com.financetracker.di

import com.financetracker.FinanceTrackerApp
import com.financetracker.data.preferences.AppPreferences
import com.financetracker.data.repository.CategoryRepository
import com.financetracker.data.repository.PaymentAccountRepository
import com.financetracker.data.repository.StatisticsRepository
import com.financetracker.data.repository.TransactionRepository

object AppModule {
    private val db get() = FinanceTrackerApp.instance.database

    val transactionRepository by lazy { TransactionRepository(db.transactionDao()) }
    val categoryRepository by lazy { CategoryRepository(db.categoryDao()) }
    val paymentAccountRepository by lazy { PaymentAccountRepository(db.paymentAccountDao()) }
    val statisticsRepository by lazy {
        StatisticsRepository(transactionRepository, categoryRepository)
    }
    val appPreferences by lazy { AppPreferences(FinanceTrackerApp.instance) }
}
