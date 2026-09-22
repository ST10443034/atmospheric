package com.group24.atmospheric.ui.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group24.atmospheric.data.repository.WeatherRepository
import com.group24.atmospheric.domain.model.WeatherInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Forecast & graph screen.
 * Role: holds the selected tab and graph variable, derives its data from [WeatherInfo].
 */
class ForecastViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ForecastUiState())
    val uiState: StateFlow<ForecastUiState> = _uiState

    fun loadWeather(lat: Double, lon: Double, locationId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getWeatherForecast(lat, lon, locationId)
                .catch { _uiState.update { s -> s.copy(isLoading = false, weather = null) } }
                .collect { weather ->
                    _uiState.update { it.copy(isLoading = false, weather = weather) }
                }
        }
    }

    fun selectTab(tab: ForecastTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun selectVariable(variable: GraphVariable) {
        _uiState.update { it.copy(selectedVariable = variable) }
    }
}

enum class ForecastTab { HOURLY, DAILY, GRAPH }

enum class GraphVariable(val label: String, val apiName: String) {
    TEMPERATURE("Temperature", "TEMPERATURE_2M"),
    PRECIPITATION("Precip %", "PRECIPITATION_PROBABILITY"),
    HUMIDITY("Humidity", "RELATIVE_HUMIDITY_2M"),
    WIND("Wind", "WIND_SPEED_10M")
}

data class ForecastUiState(
    val isLoading: Boolean = true,
    val weather: WeatherInfo? = null,
    val selectedTab: ForecastTab = ForecastTab.HOURLY,
    val selectedVariable: GraphVariable = GraphVariable.TEMPERATURE
)
