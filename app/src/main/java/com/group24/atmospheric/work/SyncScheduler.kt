package com.group24.atmospheric.work

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/** Schedules the hourly background weather sync (needs a network connection to run). */
object SyncScheduler {
    private const val TAG = "SyncScheduler"
    private const val WORK_NAME = "weather_sync"

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<WeatherSyncWorker>(1, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
            .build()
        // KEEP: don't reset the timer every time the app starts.
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        Log.d(TAG, "Hourly weather sync scheduled")
    }
}
