package com.group24.atmospheric.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Opts this device in or out of server-sent weather alerts by subscribing to / unsubscribing
 * from the FCM "weather_alerts" topic. Called when the Settings notification switch changes.
 */
object PushManager {
    private const val TAG = "PushManager"
    const val TOPIC = "weather_alerts"

    fun setEnabled(enabled: Boolean) {
        val messaging = FirebaseMessaging.getInstance()
        val task = if (enabled) messaging.subscribeToTopic(TOPIC) else messaging.unsubscribeFromTopic(TOPIC)
        task.addOnCompleteListener {
            Log.d(TAG, "FCM topic '$TOPIC' ${if (enabled) "subscribed" else "unsubscribed"}: success=${it.isSuccessful}")
        }
    }
}
