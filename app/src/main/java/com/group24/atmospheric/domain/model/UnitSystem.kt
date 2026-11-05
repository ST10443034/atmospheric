package com.group24.atmospheric.domain.model

import kotlin.math.roundToInt

/**
 * Display unit system chosen in Settings (persisted in DataStore as "METRIC" / "IMPERIAL").
 *
 * All weather data is stored and fetched in metric (Open-Meteo defaults: °C, km/h);
 * conversion happens only at display time so the cache never mixes unit systems.
 */
enum class UnitSystem(val temperatureSymbol: String, val windSymbol: String) {
    METRIC("°C", "km/h"),
    IMPERIAL("°F", "mph");

    /** Converts a Celsius value to this system's temperature scale. */
    fun temperature(celsius: Double): Double =
        if (this == IMPERIAL) celsius * 9.0 / 5.0 + 32.0 else celsius

    /** Converts a km/h value to this system's wind speed unit. */
    fun windSpeed(kmh: Double): Double =
        if (this == IMPERIAL) kmh * KMH_TO_MPH else kmh

    /** Whole-number temperature with a degree sign, e.g. "23°" or "73°". */
    fun formatTemp(celsius: Double): String = "${temperature(celsius).roundToInt()}°"

    /** Whole-number wind speed with unit, e.g. "12 km/h" or "7 mph". */
    fun formatWind(kmh: Double): String = "${windSpeed(kmh).roundToInt()} $windSymbol"

    companion object {
        private const val KMH_TO_MPH = 0.621371

        /** Parses the DataStore value; anything unknown falls back to METRIC. */
        fun fromKey(key: String?): UnitSystem =
            entries.firstOrNull { it.name.equals(key, ignoreCase = true) } ?: METRIC
    }
}
