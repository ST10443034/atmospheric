package com.group24.atmospheric.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Combined DAO for all database operations.
 */
@Dao
interface WeatherDao {

    // User Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE userId = :userId")
    fun getUser(userId: String): Flow<UserEntity?>

    // Weather Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherEntity)

    @Query("SELECT * FROM weather WHERE locationId = :locationId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestWeather(locationId: Long): Flow<WeatherEntity?>

    // Location Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationEntity)

    @Query("SELECT * FROM locations WHERE userId = :userId")
    fun getLocationsForUser(userId: String): Flow<List<LocationEntity>>

    // Sync Queue Operations
    @Insert
    suspend fun addToQueue(item: SyncQueueEntity)

    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY createdAt ASC")
    fun getPendingItems(): Flow<List<SyncQueueEntity>>

    // FCM Token Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToken(token: FCMTokenEntity)
}
