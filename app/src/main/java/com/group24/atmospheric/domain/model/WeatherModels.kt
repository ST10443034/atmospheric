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
 *
 * [isCached] and [fetchedAt] carry the freshness signal the design depends on: whether this
 * reading came from the network just now, or from the last Room row written when we were online.
 */
data class WeatherInfo(
    val current: CurrentWeather,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>,
    val isCached: Boolean = false,
    val fetchedAt: Long = 0L
)
