package com.group24.atmospheric.ui.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group24.atmospheric.data.repository.WeatherRepository
import com.group24.atmospheric.domain.model.WeatherInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel for the Dashboard screen.
 * Role: Manages weather data flow and UI state.
 */
class DashboardViewModel(private val repository: WeatherRepository) : ViewModel() {
    private val TAG = "DashboardViewModel"

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState

    /**
     * Loads weather for a specific location.
     */
    fun loadWeather(lat: Double, lon: Double, locationId: Long) {
        viewModelScope.launch {
            Log.d(TAG, "Loading weather for locationId: $locationId")
            _uiState.value = DashboardUiState.Loading
            
            repository.getWeatherForecast(lat, lon, locationId)
                .catch { e ->
                    Log.e(TAG, "Flow error: ${e.message}")
                    _uiState.value = DashboardUiState.Error(e.message ?: "Unknown error")
                }
                .collect { weatherInfo ->
                    if (weatherInfo != null) {
                        Log.d(TAG, "Weather data received successfully")
                        _uiState.value = DashboardUiState.Success(weatherInfo)
                    } else {
                        Log.e(TAG, "Weather data is null")
                        _uiState.value = DashboardUiState.Error("Could not load weather data")
                    }
                }
        }
    }
}

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val data: WeatherInfo) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}
