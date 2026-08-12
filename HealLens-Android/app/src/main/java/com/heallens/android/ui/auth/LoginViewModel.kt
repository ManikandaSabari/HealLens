package com.heallens.android.ui.auth

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.heallens.android.data.local.DataStoreManager
import com.heallens.android.data.repository.AuthRepository
import com.heallens.android.data.repository.AuthRepositoryImpl
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import android.util.Log

class LoginViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val dataStoreManager = DataStoreManager(application)
    private val authRepository: AuthRepository = AuthRepositoryImpl(dataStoreManager = dataStoreManager)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<LoginEvent>()
    val eventFlow: SharedFlow<LoginEvent> = _eventFlow.asSharedFlow()

    fun onEmailChanged(newEmail: String) {
        _uiState.value = _uiState.value.copy(
            emailInput = newEmail,
            isEmailError = false,
            errorMessage = null
        )
    }

    fun onPasswordChanged(newPassword: String) {
        _uiState.value = _uiState.value.copy(
            passwordInput = newPassword,
            isPasswordError = false,
            errorMessage = null
        )
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isPasswordVisible = !_uiState.value.isPasswordVisible
        )
    }

    fun fillDemoCredentials(email: String, pass: String) {
        _uiState.value = _uiState.value.copy(
            emailInput = email,
            passwordInput = pass,
            isEmailError = false,
            isPasswordError = false,
            errorMessage = null
        )
    }

    fun performLogin() {
        val email = _uiState.value.emailInput.trim()
        val password = _uiState.value.passwordInput.trim()

        if (email.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                isEmailError = true,
                errorMessage = "Please enter your email address."
            )
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = _uiState.value.copy(
                isEmailError = true,
                errorMessage = "Please enter a valid email format."
            )
            return
        }

        if (password.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                isPasswordError = true,
                errorMessage = "Please enter your password."
            )
            return
        }

        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(
                isPasswordError = true,
                errorMessage = "Password must be at least 6 characters."
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {
            val result = authRepository.login(email, password)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true
                )
                _eventFlow.emit(LoginEvent.NavigateToDashboard)
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Invalid login credentials. Please try again."
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = errorMsg
                )
            }
        }
    }

    fun onSignupClicked() {
        viewModelScope.launch {
            _eventFlow.emit(LoginEvent.NavigateToSignup)
        }
    }

    fun onForgotPasswordClicked() {
        viewModelScope.launch {
            _eventFlow.emit(LoginEvent.NavigateToForgotPassword)
        }
    }

    fun performGoogleLoginWithAccount(idToken: String?, email: String?, googleId: String?) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {
            Log.d("GoogleDebug", "[GoogleDebug] Google authentication result starting")
            val result = authRepository.signInWithGoogleAccount(idToken, email, googleId)
            if (result.isSuccess) {
                Log.d("GoogleDebug", "[GoogleDebug] Login success state changed")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true
                )
                Log.d("GoogleDebug", "[GoogleDebug] Navigation event emitted")
                _eventFlow.emit(LoginEvent.NavigateToDashboard)
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Google Sign-In failed. Please try again."
                Log.e("GoogleDebug", "[GoogleDebug] ERROR: $errorMsg")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = errorMsg
                )
            }
        }
    }

    fun onGoogleSignInError(message: String) {
        Log.e("GoogleDebug", "[GoogleDebug] ERROR: $message")
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = message
        )
    }

    fun onGoogleSignInCancelled() {
        Log.d("GoogleDebug", "[GoogleDebug] Sign-in cancelled by user")
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = null
        )
    }
}
