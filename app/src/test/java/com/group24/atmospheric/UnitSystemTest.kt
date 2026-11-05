package com.group24.atmospheric

import com.group24.atmospheric.domain.model.UnitSystem
import org.junit.Assert.assertEquals
import org.junit.Test

/** Verifies the metric/imperial conversion used by every weather display. */
class UnitSystemTest {

    @Test
    fun metric_leavesValuesUnchanged() {
        assertEquals(23.0, UnitSystem.METRIC.temperature(23.0), 0.0001)
        assertEquals(12.0, UnitSystem.METRIC.windSpeed(12.0), 0.0001)
    }

    @Test
    fun imperial_convertsCelsiusToFahrenheit() {
        assertEquals(32.0, UnitSystem.IMPERIAL.temperature(0.0), 0.0001)
        assertEquals(212.0, UnitSystem.IMPERIAL.temperature(100.0), 0.0001)
        assertEquals(-40.0, UnitSystem.IMPERIAL.temperature(-40.0), 0.0001)
    }

    @Test
    fun imperial_convertsKmhToMph() {
        assertEquals(62.1371, UnitSystem.IMPERIAL.windSpeed(100.0), 0.001)
    }

    @Test
    fun formatting_includesCorrectSymbols() {
        assertEquals("23°", UnitSystem.METRIC.formatTemp(23.2))
        assertEquals("73°", UnitSystem.IMPERIAL.formatTemp(23.0))
        assertEquals("12 km/h", UnitSystem.METRIC.formatWind(12.0))
        assertEquals("7 mph", UnitSystem.IMPERIAL.formatWind(12.0))
        assertEquals("°F", UnitSystem.IMPERIAL.temperatureSymbol)
    }

    @Test
    fun formatting_roundsInsteadOfTruncating() {
        assertEquals("24°", UnitSystem.METRIC.formatTemp(23.6))
        assertEquals("-1°", UnitSystem.METRIC.formatTemp(-0.6))
    }

    @Test
    fun fromKey_parsesStoredValueAndDefaultsToMetric() {
        assertEquals(UnitSystem.IMPERIAL, UnitSystem.fromKey("IMPERIAL"))
        assertEquals(UnitSystem.METRIC, UnitSystem.fromKey("METRIC"))
        assertEquals(UnitSystem.METRIC, UnitSystem.fromKey(null))
        assertEquals(UnitSystem.METRIC, UnitSystem.fromKey("garbage"))
    }
}
