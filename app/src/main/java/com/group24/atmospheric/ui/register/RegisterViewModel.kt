package com.group24.atmospheric.ui.register

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group24.atmospheric.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Registration screen.
 * Role: Handles new-account input, validation, and creation.
 */
class RegisterViewModel(private val repository: AuthRepository) : ViewModel() {
    private val TAG = "RegisterViewModel"

    private val _registerState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val registerState: StateFlow<RegisterUiState> = _registerState

    fun register(email: String, pass: String, confirmPass: String) {
        if (!validateInput(email, pass, confirmPass)) return

        viewModelScope.launch {
            _registerState.value = RegisterUiState.Loading
            Log.d(TAG, "Registration initiated for $email")

            val result = repository.signUp(email, pass)
            result.onSuccess {
                Log.d(TAG, "Registration successful")
                _registerState.value = RegisterUiState.Success
            }.onFailure { error ->
                Log.e(TAG, "Registration failed: ${error.message}")
                _registerState.value = RegisterUiState.Error(error.message ?: "Unknown error")
            }
        }
    }

    private fun validateInput(email: String, pass: String, confirmPass: String): Boolean {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _registerState.value = RegisterUiState.Error("Invalid email address")
            return false
        }
        if (pass.length < 6) {
            _registerState.value = RegisterUiState.Error("Password must be at least 6 characters")
            return false
        }
        if (pass != confirmPass) {
            _registerState.value = RegisterUiState.Error("Passwords do not match")
            return false
        }
        return true
    }
}

sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    object Success : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}
