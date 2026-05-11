package com.financetracker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AppPreferences(private val context: Context) {

    companion object {
        private val KEY_NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        private val KEY_LAST_ACCOUNT_ID = longPreferencesKey("last_account_id")
    }

    val isNotificationEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_NOTIFICATION_ENABLED] ?: false
    }

    val lastAccountId: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[KEY_LAST_ACCOUNT_ID] ?: -1L
    }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        context.dataStore.updateData { prefs ->
            prefs.toMutablePreferences().apply {
                set(KEY_NOTIFICATION_ENABLED, enabled)
            }
        }
    }

    suspend fun setLastAccountId(id: Long) {
        context.dataStore.updateData { prefs ->
            prefs.toMutablePreferences().apply {
                set(KEY_LAST_ACCOUNT_ID, id)
            }
        }
    }
}
