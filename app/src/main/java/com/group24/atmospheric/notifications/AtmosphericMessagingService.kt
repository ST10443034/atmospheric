package com.group24.atmospheric.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.group24.atmospheric.data.local.DataStoreManager
import com.group24.atmospheric.data.repository.UserProfileRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Receives Firebase Cloud Messaging pushes while the app is in the foreground and shows them
 * as notifications, but only when the user has the Settings notification switch turned on.
 * (When the app is in the background FCM displays notification-payload messages itself.)
 */
class AtmosphericMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "Push received from ${message.from}")
        val enabled = runBlocking { DataStoreManager(applicationContext).notificationsEnabled.first() }
        if (!enabled) {
            Log.d(TAG, "Ignoring push: notifications switched off in Settings")
            return
        }
        val title = message.notification?.title ?: message.data["title"] ?: return
        val body = message.notification?.body ?: message.data["body"].orEmpty()
        NotificationHelper.show(applicationContext, System.currentTimeMillis().toInt(), title, body)
    }

    /** A new/rotated token is stored against the signed-in user so a server could target them. */
    override fun onNewToken(token: String) {
        Log.d(TAG, "New FCM token received")
        UserProfileRepository().saveFcmToken(token)
    }

    private companion object {
        const val TAG = "MessagingService"
    }
}
