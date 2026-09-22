package com.group24.atmospheric.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group24.atmospheric.data.local.DataStoreManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Settings screen.
 * Role: Bridges DataStore and UI for preference persistence.
 */
class SettingsViewModel(private val dataStoreManager: DataStoreManager) : ViewModel() {

    val themeMode: StateFlow<String> = dataStoreManager.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")

    val units: StateFlow<String> = dataStoreManager.units
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "METRIC")

    val notificationsEnabled: StateFlow<Boolean> = dataStoreManager.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun updateTheme(mode: String) {
        viewModelScope.launch { dataStoreManager.setThemeMode(mode) }
    }

    fun updateUnits(unit: String) {
        viewModelScope.launch { dataStoreManager.setUnits(unit) }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch { dataStoreManager.setNotificationsEnabled(enabled) }
    }
}
