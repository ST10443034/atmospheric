package com.group24.atmospheric.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.group24.atmospheric.R
import com.group24.atmospheric.data.local.AppDatabase
import com.group24.atmospheric.data.local.DataStoreManager
import com.group24.atmospheric.data.remote.OpenMeteoApiService
import com.group24.atmospheric.data.remote.toDomainModel
import com.group24.atmospheric.data.repository.toEntity
import com.group24.atmospheric.domain.model.AlertRules
import com.group24.atmospheric.domain.model.AlertType
import com.group24.atmospheric.domain.model.DefaultLocation
import com.group24.atmospheric.domain.model.UnitSystem
import com.group24.atmospheric.notifications.NotificationHelper
import kotlinx.coroutines.flow.first
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Periodic background job (WorkManager, runs only with a network connection):
 *  1. fetches the latest forecast from Open-Meteo and refreshes the Room cache, and
 *  2. if the Settings notification switch is on, raises a local alert for severe conditions.
 * Failures use WorkManager's exponential backoff retry, then give up until the next period.
 */
class WeatherSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Background sync started (attempt ${runAttemptCount + 1})")
        return try {
            val api = Retrofit.Builder()
                .baseUrl(OpenMeteoApiService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OpenMeteoApiService::class.java)

            val weather = api.getForecast(DefaultLocation.LATITUDE, DefaultLocation.LONGITUDE).toDomainModel()
            AppDatabase.getDatabase(applicationContext).weatherDao()
                .insertWeather(weather.current.toEntity(DefaultLocation.ID))
            Log.d(TAG, "Cache refreshed")

            val prefs = DataStoreManager(applicationContext)
            if (prefs.notificationsEnabled.first()) {
                AlertRules.evaluate(weather.current)?.let { alert ->
                    val units = UnitSystem.fromKey(prefs.units.first())
                    val (title, id) = titleFor(alert)
                    val body = applicationContext.getString(
                        R.string.alert_body,
                        DefaultLocation.NAME,
                        units.formatTemp(weather.current.temperature) + units.temperatureSymbol.removePrefix("°"),
                        units.formatWind(weather.current.windSpeed)
                    )
                    NotificationHelper.show(applicationContext, id, applicationContext.getString(title), body)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Background sync failed: ${e.message}")
            if (runAttemptCount < MAX_ATTEMPTS) Result.retry() else Result.failure()
        }
    }

    private fun titleFor(alert: AlertType): Pair<Int, Int> = when (alert) {
        AlertType.THUNDERSTORM -> R.string.alert_title_thunderstorm to 1001
        AlertType.EXTREME_HEAT -> R.string.alert_title_heat to 1002
        AlertType.HEAVY_RAIN -> R.string.alert_title_rain to 1003
        AlertType.STRONG_WIND -> R.string.alert_title_wind to 1004
    }

    private companion object {
        const val TAG = "WeatherSyncWorker"
        const val MAX_ATTEMPTS = 3
    }
}
