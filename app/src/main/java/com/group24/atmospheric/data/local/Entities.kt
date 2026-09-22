package com.group24.atmospheric.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val email: String,
    val displayName: String
)

@Entity(tableName = "weather")
data class WeatherEntity(
    @PrimaryKey(autoGenerate = true) val weatherId: Long = 0,
    val locationId: Long,
    val timestamp: Long,
    val temperature: Double,
    val apparentTemperature: Double,
    val humidity: Double,
    val windSpeed: Double,
    val precipitationProbability: Double,
    val weatherCode: Int
)

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true) val locationId: Long = 0,
    val userId: String,
    val name: String,
    val latitude: Double,
    val longitude: Double
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val queueId: Long = 0,
    val operation: String,
    val payload: String,
    val status: String,
    val retryCount: Int,
    val createdAt: Long
)

@Entity(tableName = "fcm_tokens")
data class FCMTokenEntity(
    @PrimaryKey(autoGenerate = true) val tokenId: Long = 0,
    val userId: String,
    val token: String,
    val createdAt: Long
)
