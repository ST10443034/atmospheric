package com.group24.atmospheric.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Manages app settings using Jetpack DataStore Preferences.
 */
class DataStoreManager(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val UNITS = stringPreferencesKey("units")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    val themeMode: Flow<String> = context.dataStore.data.map { pref ->
        pref[Keys.THEME_MODE] ?: "SYSTEM"
    }

    val units: Flow<String> = context.dataStore.data.map { pref ->
        pref[Keys.UNITS] ?: "METRIC"
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { pref ->
        pref[Keys.NOTIFICATIONS_ENABLED] ?: true
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    suspend fun setUnits(unit: String) {
        context.dataStore.edit { it[Keys.UNITS] = unit }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }
}
