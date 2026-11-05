package com.group24.atmospheric.domain.model

/** Kinds of severe-weather alert the app can raise. */
enum class AlertType { THUNDERSTORM, EXTREME_HEAT, HEAVY_RAIN, STRONG_WIND }

/**
 * Pure decision logic for "is this reading severe enough to alert the user?".
 * Kept free of Android classes so it can be unit tested on the JVM.
 * Inputs are metric (°C, km/h, % precipitation probability), as stored in Room.
 */
object AlertRules {
    const val HEAT_THRESHOLD_C = 35.0
    const val RAIN_THRESHOLD_PERCENT = 80.0
    const val WIND_THRESHOLD_KMH = 50.0

    /** Returns the most severe alert for [current], or null when conditions are ordinary. */
    fun evaluate(current: CurrentWeather): AlertType? = when {
        current.weatherCode in 95..99 -> AlertType.THUNDERSTORM
        current.temperature >= HEAT_THRESHOLD_C -> AlertType.EXTREME_HEAT
        current.precipitation >= RAIN_THRESHOLD_PERCENT -> AlertType.HEAVY_RAIN
        current.windSpeed >= WIND_THRESHOLD_KMH -> AlertType.STRONG_WIND
        else -> null
    }
}

/** The app's fixed default location (Johannesburg), shared by the UI and background worker. */
object DefaultLocation {
    const val NAME = "Johannesburg"
    const val LATITUDE = -26.2041
    const val LONGITUDE = 28.0473
    const val ID = 1L
}
