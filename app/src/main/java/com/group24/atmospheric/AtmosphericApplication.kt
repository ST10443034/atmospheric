package com.group24.atmospheric

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.group24.atmospheric.data.local.DataStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Applies the persisted theme (dark by default, per the design spec) before any
 * Activity is created, so the very first frame is already in the right mode.
 */
class AtmosphericApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val dataStoreManager = DataStoreManager(this)
        CoroutineScope(Dispatchers.Main).launch {
            val mode = dataStoreManager.themeMode.first()
            AppCompatDelegate.setDefaultNightMode(
                when (mode) {
                    "LIGHT" -> AppCompatDelegate.MODE_NIGHT_NO
                    "SYSTEM" -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    else -> AppCompatDelegate.MODE_NIGHT_YES
                }
            )
        }
    }
}
