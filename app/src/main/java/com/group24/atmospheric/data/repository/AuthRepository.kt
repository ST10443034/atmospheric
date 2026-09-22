package com.group24.atmospheric.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

/**
 * Repository for Firebase Authentication.
 * Role: Encapsulates Firebase Auth logic.
 */
class AuthRepository(private val firebaseAuth: FirebaseAuth) {
    private val TAG = "AuthRepository"

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    /**
     * Signs in with email and password.
     */
    suspend fun signIn(email: String, pass: String): Result<FirebaseUser?> {
        return try {
            Log.d(TAG, "Attempting login for: $email")
            val result = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
            Result.success(result.user)
        } catch (e: Exception) {
            Log.e(TAG, "Login failed: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Registers a new user with email and password.
     */
    suspend fun signUp(email: String, pass: String): Result<FirebaseUser?> {
        return try {
            Log.d(TAG, "Attempting registration for: $email")
            val result = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
            Result.success(result.user)
        } catch (e: Exception) {
            Log.e(TAG, "Registration failed: ${e.message}")
            Result.failure(e)
        }
    }

    fun signOut() {
        Log.d(TAG, "Signing out user")
        firebaseAuth.signOut()
    }
}
