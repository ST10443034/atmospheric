package com.group24.atmospheric.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group24.atmospheric.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Login screen.
 * Role: Handles authentication input and validation.
 */
class LoginViewModel(private val repository: AuthRepository) : ViewModel() {
    private val TAG = "LoginViewModel"

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState

    fun login(email: String, pass: String) {
        if (!validateInput(email, pass)) return

        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading
            Log.d(TAG, "Login initiated for $email")
            
            val result = repository.signIn(email, pass)
            result.onSuccess {
                Log.d(TAG, "Login successful")
                _loginState.value = LoginUiState.Success
            }.onFailure { error ->
                Log.e(TAG, "Login failed: ${error.message}")
                _loginState.value = LoginUiState.Error(error.message ?: "Unknown error")
            }
        }
    }

    private fun validateInput(email: String, pass: String): Boolean {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _loginState.value = LoginUiState.Error("Invalid email address")
            return false
        }
        if (pass.length < 6) {
            _loginState.value = LoginUiState.Error("Password must be at least 6 characters")
            return false
        }
        return true
    }
}

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
