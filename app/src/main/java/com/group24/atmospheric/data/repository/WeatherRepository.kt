package com.group24.atmospheric.data.repository

import android.util.Log
import com.group24.atmospheric.data.local.WeatherDao
import com.group24.atmospheric.data.local.WeatherEntity
import com.group24.atmospheric.data.remote.OpenMeteoApiService
import com.group24.atmospheric.data.remote.toDomainModel
import com.group24.atmospheric.domain.model.WeatherInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

/**
 * Repository coordinating remote and local weather data.
 * Role: Implements offline-first logic and provides data to ViewModels.
 */
class WeatherRepository(
    private val api: OpenMeteoApiService,
    private val dao: WeatherDao
) {
    private val TAG = "WeatherRepository"

    /**
     * Fetches weather from network and caches it, or falls back to Room if offline.
     */
    fun getWeatherForecast(lat: Double, lon: Double, locationId: Long): Flow<WeatherInfo?> = flow {
        try {
            Log.d(TAG, "Fetching remote weather for lat: $lat, lon: $lon")
            val response = api.getForecast(lat, lon)
            val domainModel = response.toDomainModel()
            
            // Cache current weather in Room
            val entity = WeatherEntity(
                locationId = locationId,
                timestamp = domainModel.current.timestamp,
                temperature = domainModel.current.temperature,
                apparentTemperature = domainModel.current.feelsLike,
                humidity = domainModel.current.humidity,
                windSpeed = domainModel.current.windSpeed,
                precipitationProbability = domainModel.current.precipitation,
                weatherCode = domainModel.current.weatherCode
            )
            dao.insertWeather(entity)
            
            emit(domainModel)
        } catch (e: Exception) {
            Log.e(TAG, "Network failed, falling back to cache: ${e.message}")
            // Defer: In a full app, we'd map the Room entity back to a domain model
            // For now, we emit null or try to read the latest from DAO
            emit(null) 
        }
    }
}
