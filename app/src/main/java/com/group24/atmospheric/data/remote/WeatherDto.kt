package com.group24.atmospheric.data.remote

import com.google.gson.annotations.SerializedName
import com.group24.atmospheric.domain.model.CurrentWeather
import com.group24.atmospheric.domain.model.DailyForecast
import com.group24.atmospheric.domain.model.HourlyForecast
import com.group24.atmospheric.domain.model.WeatherInfo
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Data Transfer Objects (DTOs) for Open-Meteo API response mapping.
 */
data class WeatherResponseDto(
    @SerializedName("current") val current: CurrentDto,
    @SerializedName("hourly") val hourly: HourlyDto,
    @SerializedName("daily") val daily: DailyDto
)

data class CurrentDto(
    @SerializedName("time") val time: String,
    @SerializedName("temperature_2m") val temperature: Double,
    @SerializedName("relative_humidity_2m") val humidity: Double,
    @SerializedName("apparent_temperature") val apparentTemperature: Double,
    @SerializedName("precipitation") val precipitation: Double,
    @SerializedName("weather_code") val weatherCode: Int,
    @SerializedName("wind_speed_10m") val windSpeed: Double
)

data class HourlyDto(
    @SerializedName("time") val time: List<String>,
    @SerializedName("temperature_2m") val temperature: List<Double>,
    @SerializedName("precipitation_probability") val precipitationProbability: List<Int>,
    @SerializedName("weather_code") val weatherCode: List<Int>
)

data class DailyDto(
    @SerializedName("time") val time: List<String>,
    @SerializedName("weather_code") val weatherCode: List<Int>,
    @SerializedName("temperature_2m_max") val maxTemp: List<Double>,
    @SerializedName("temperature_2m_min") val minTemp: List<Double>
)

/**
 * Extension function to map DTO to clean domain model.
 */
fun WeatherResponseDto.toDomainModel(): WeatherInfo {
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    val currentModel = CurrentWeather(
        temperature = current.temperature,
        feelsLike = current.apparentTemperature,
        humidity = current.humidity,
        windSpeed = current.windSpeed,
        precipitation = current.precipitation,
        weatherCode = current.weatherCode,
        timestamp = LocalDateTime.parse(current.time, formatter).toEpochSecond(ZoneOffset.UTC)
    )

    val hourlyList = hourly.time.indices.map { i ->
        HourlyForecast(
            time = LocalDateTime.parse(hourly.time[i], formatter).toEpochSecond(ZoneOffset.UTC),
            temperature = hourly.temperature[i],
            precipitationProbability = hourly.precipitationProbability[i],
            weatherCode = hourly.weatherCode[i]
        )
    }

    val dailyList = daily.time.indices.map { i ->
        DailyForecast(
            date = LocalDateTime.parse("${daily.time[i]}T00:00:00", formatter).toEpochSecond(ZoneOffset.UTC),
            maxTemp = daily.maxTemp[i],
            minTemp = daily.minTemp[i],
            weatherCode = daily.weatherCode[i]
        )
    }

    return WeatherInfo(
        current = currentModel,
        hourly = hourlyList,
        daily = dailyList
    )
}
