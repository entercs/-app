package com.financetracker

import android.app.Application

class FinanceTrackerApp : Application() {
    lateinit var database: com.financetracker.data.db.AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = com.financetracker.data.db.AppDatabase.create(this)
    }

    companion object {
        lateinit var instance: FinanceTrackerApp
            private set
    }
}
