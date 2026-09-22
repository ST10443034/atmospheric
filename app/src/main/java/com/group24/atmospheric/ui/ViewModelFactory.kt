package com.group24.atmospheric.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.group24.atmospheric.data.local.AppDatabase
import com.group24.atmospheric.data.local.DataStoreManager
import com.group24.atmospheric.data.remote.OpenMeteoApiService
import com.group24.atmospheric.data.repository.AuthRepository
import com.group24.atmospheric.data.repository.WeatherRepository
import com.group24.atmospheric.ui.dashboard.DashboardViewModel
import com.group24.atmospheric.ui.login.LoginViewModel
import com.group24.atmospheric.ui.register.RegisterViewModel
import com.group24.atmospheric.ui.settings.SettingsViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Manual Dependency Injection using ViewModelProvider.Factory.
 * Role: Instantiates ViewModels with required dependencies.
 */
class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    private val retrofit = Retrofit.Builder()
        .baseUrl(OpenMeteoApiService.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(OpenMeteoApiService::class.java)
    private val database = AppDatabase.getDatabase(context)
    private val dataStoreManager = DataStoreManager(context)

    private val weatherRepository = WeatherRepository(apiService, database.weatherDao())
    private val authRepository = AuthRepository(FirebaseAuth.getInstance())

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(authRepository) as T
            }
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(authRepository) as T
            }
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(weatherRepository) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(dataStoreManager) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
