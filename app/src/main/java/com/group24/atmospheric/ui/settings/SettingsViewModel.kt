package com.group24.atmospheric.ui.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group24.atmospheric.data.local.DataStoreManager
import com.group24.atmospheric.data.repository.AuthRepository
import com.group24.atmospheric.data.repository.UserProfile
import com.group24.atmospheric.data.repository.UserProfileRepository
import com.group24.atmospheric.notifications.PushManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Settings screen.
 * Role: Bridges DataStore (local preferences), Firestore (synced profile/prefs) and the UI.
 */
class SettingsViewModel(
    private val dataStoreManager: DataStoreManager,
    private val authRepository: AuthRepository,
    private val profileRepository: UserProfileRepository
) : ViewModel() {

    val themeMode: StateFlow<String> = dataStoreManager.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "DARK")

    val units: StateFlow<String> = dataStoreManager.units
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "METRIC")

    val notificationsEnabled: StateFlow<Boolean> = dataStoreManager.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val language: StateFlow<String> = dataStoreManager.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "ENGLISH")

    /** The Firestore profile (name + photo); null until loaded or when signed out/offline with no cache. */
    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch { _profile.value = profileRepository.loadProfile() }
    }

    fun saveProfile(displayName: String, photoBase64: String) {
        viewModelScope.launch {
            val updated = UserProfile(
                displayName = displayName.trim(),
                photoBase64 = photoBase64,
                units = dataStoreManager.units.first(),
                language = dataStoreManager.language.first(),
                theme = dataStoreManager.themeMode.first(),
                notificationsEnabled = dataStoreManager.notificationsEnabled.first()
            )
            profileRepository.saveProfile(updated)
            _profile.value = updated
            Log.d(TAG, "Profile saved (name length=${updated.displayName.length}, photo=${photoBase64.length} chars)")
        }
    }

    fun updateTheme(mode: String) {
        viewModelScope.launch { dataStoreManager.setThemeMode(mode) }
        profileRepository.savePreference("theme", mode)
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
            when (mode) {
                "LIGHT" -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
                "SYSTEM" -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                else -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
            }
        )
    }

    fun updateUnits(unit: String) {
        viewModelScope.launch { dataStoreManager.setUnits(unit) }
        profileRepository.savePreference("units", unit)
    }

    /**
     * Turning notifications on/off actually changes behaviour: it subscribes/unsubscribes the device
     * from the FCM "weather_alerts" topic, and the background worker only raises local alerts while on.
     */
    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch { dataStoreManager.setNotificationsEnabled(enabled) }
        PushManager.setEnabled(enabled)
        profileRepository.savePreference("notificationsEnabled", enabled)
        Log.d(TAG, "Notifications ${if (enabled) "enabled" else "disabled"}")
    }

    fun updateLanguage(language: String) {
        viewModelScope.launch { dataStoreManager.setLanguage(language) }
        profileRepository.savePreference("language", language)
    }

    fun logout() {
        authRepository.signOut()
    }

    private companion object {
        const val TAG = "SettingsViewModel"
    }
}
