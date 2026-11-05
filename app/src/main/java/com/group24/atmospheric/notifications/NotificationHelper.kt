package com.group24.atmospheric.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.group24.atmospheric.MainActivity
import com.group24.atmospheric.R
import com.group24.atmospheric.ui.login.LoginActivity

/**
 * Creates the weather-alert notification channel and posts notifications.
 * Used by both the WorkManager alert check and the FCM push service.
 */
object NotificationHelper {
    private const val TAG = "NotificationHelper"
    const val CHANNEL_ID = "weather_alerts"

    /** Android 8+ requires a channel; safe to call repeatedly. */
    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notif_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = context.getString(R.string.notif_channel_desc) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /** Android 13+ needs the runtime POST_NOTIFICATIONS permission; earlier versions always allow. */
    fun canPost(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission") // guarded by canPost()
    fun show(context: Context, id: Int, title: String, body: String) {
        if (!canPost(context)) {
            Log.w(TAG, "Notification skipped: permission not granted")
            return
        }
        ensureChannel(context)
        // Tapping opens the dashboard if signed in, otherwise the login screen.
        val target = if (FirebaseAuth.getInstance().currentUser != null) MainActivity::class.java
        else LoginActivity::class.java
        val pending = PendingIntent.getActivity(
            context, 0, Intent(context, target),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_nav_home)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
        Log.d(TAG, "Posted notification: $title")
    }
}
