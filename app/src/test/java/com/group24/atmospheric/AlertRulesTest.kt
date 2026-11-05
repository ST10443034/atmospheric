package com.group24.atmospheric

import com.group24.atmospheric.domain.model.AlertRules
import com.group24.atmospheric.domain.model.AlertType
import com.group24.atmospheric.domain.model.CurrentWeather
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Verifies which readings trigger a severe-weather notification. */
class AlertRulesTest {

    private fun reading(
        temp: Double = 22.0,
        wind: Double = 10.0,
        precip: Double = 10.0,
        code: Int = 1
    ) = CurrentWeather(
        temperature = temp, feelsLike = temp, humidity = 40.0, windSpeed = wind,
        precipitation = precip, weatherCode = code, timestamp = 0L
    )

    @Test
    fun ordinaryWeather_raisesNoAlert() {
        assertNull(AlertRules.evaluate(reading()))
    }

    @Test
    fun thunderstormCodes_raiseThunderstormAlert() {
        assertEquals(AlertType.THUNDERSTORM, AlertRules.evaluate(reading(code = 95)))
        assertEquals(AlertType.THUNDERSTORM, AlertRules.evaluate(reading(code = 99)))
        assertNull(AlertRules.evaluate(reading(code = 94)))
    }

    @Test
    fun heatAtThreshold_raisesHeatAlert() {
        assertEquals(AlertType.EXTREME_HEAT, AlertRules.evaluate(reading(temp = 35.0)))
        assertNull(AlertRules.evaluate(reading(temp = 34.9)))
    }

    @Test
    fun highRainProbability_raisesRainAlert() {
        assertEquals(AlertType.HEAVY_RAIN, AlertRules.evaluate(reading(precip = 80.0)))
        assertNull(AlertRules.evaluate(reading(precip = 79.0)))
    }

    @Test
    fun strongWind_raisesWindAlert() {
        assertEquals(AlertType.STRONG_WIND, AlertRules.evaluate(reading(wind = 50.0)))
        assertNull(AlertRules.evaluate(reading(wind = 49.0)))
    }

    @Test
    fun thunderstorm_takesPriorityOverOtherConditions() {
        assertEquals(
            AlertType.THUNDERSTORM,
            AlertRules.evaluate(reading(temp = 40.0, wind = 80.0, precip = 100.0, code = 96))
        )
    }
}
