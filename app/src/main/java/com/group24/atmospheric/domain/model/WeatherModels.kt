package com.group24.atmospheric.domain.model

/**
 * Clean domain model for current weather conditions.
 */
data class CurrentWeather(
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Double,
    val windSpeed: Double,
    val precipitation: Double,
    val weatherCode: Int,
    val timestamp: Long
)

/**
 * Clean domain model for an hourly forecast entry.
 */
data class HourlyForecast(
    val time: Long,
    val temperature: Double,
    val precipitationProbability: Int,
    val weatherCode: Int
)

/**
 * Clean domain model for a daily forecast entry.
 */
data class DailyForecast(
    val date: Long,
    val maxTemp: Double,
    val minTemp: Double,
    val weatherCode: Int
)

/**
 * Aggregated weather information for the UI.
 */
data class WeatherInfo(
    val current: CurrentWeather,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>
)
