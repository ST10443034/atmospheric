package com.group24.atmospheric.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/** The user's profile as stored in the Cloud Firestore (NoSQL) document `users/{uid}`. */
data class UserProfile(
    val displayName: String = "",
    /** Profile photo kept as a small Base64 JPEG (blob data) so no paid Cloud Storage is needed. */
    val photoBase64: String = "",
    val units: String = "METRIC",
    val language: String = "ENGLISH",
    val theme: String = "DARK",
    val notificationsEnabled: Boolean = false
)

/**
 * NoSQL storage (Cloud Firestore) for per-user data.
 *
 * Firestore keeps its own offline cache: writes made without a connection are queued and
 * uploaded automatically on reconnect, so write calls here are intentionally fire-and-forget
 * (awaiting them would block until the server acknowledged).
 */
class UserProfileRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private fun userDoc() = auth.currentUser?.uid?.let { db.collection("users").document(it) }

    /** Reads the profile (from the server, or Firestore's local cache when offline). */
    suspend fun loadProfile(): UserProfile? {
        val doc = userDoc() ?: return null
        return try {
            val d = doc.get().await()
            if (!d.exists()) null else UserProfile(
                displayName = d.getString("displayName").orEmpty(),
                photoBase64 = d.getString("photoBase64").orEmpty(),
                units = d.getString("units") ?: "METRIC",
                language = d.getString("language") ?: "ENGLISH",
                theme = d.getString("theme") ?: "DARK",
                notificationsEnabled = d.getBoolean("notificationsEnabled") ?: false
            )
        } catch (e: Exception) {
            Log.e(TAG, "Could not read profile: ${e.message}")
            null
        }
    }

    /** Merges the given profile fields into the user's document. */
    fun saveProfile(profile: UserProfile) {
        val doc = userDoc() ?: return
        val data = mapOf(
            "displayName" to profile.displayName,
            "photoBase64" to profile.photoBase64,
            "units" to profile.units,
            "language" to profile.language,
            "theme" to profile.theme,
            "notificationsEnabled" to profile.notificationsEnabled,
            "email" to auth.currentUser?.email,
            "updatedAt" to System.currentTimeMillis()
        )
        doc.set(data, SetOptions.merge())
            .addOnSuccessListener { Log.d(TAG, "Profile synced to Firestore") }
            .addOnFailureListener { Log.e(TAG, "Profile sync failed: ${it.message}") }
    }

    /** Stores a single preference change so settings follow the user across devices. */
    fun savePreference(field: String, value: Any) {
        userDoc()?.set(mapOf(field to value, "updatedAt" to System.currentTimeMillis()), SetOptions.merge())
            ?.addOnFailureListener { Log.e(TAG, "Preference sync failed: ${it.message}") }
    }

    /** Saves the device's FCM registration token against the signed-in user. */
    fun saveFcmToken(token: String) = savePreference("fcmToken", token)

    private companion object {
        const val TAG = "UserProfileRepository"
    }
}
