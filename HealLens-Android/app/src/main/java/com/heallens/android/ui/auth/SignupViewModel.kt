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

class SignupViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val dataStoreManager = DataStoreManager(application)
    private val authRepository: AuthRepository = AuthRepositoryImpl(dataStoreManager = dataStoreManager)

    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<SignupEvent>()
    val eventFlow: SharedFlow<SignupEvent> = _eventFlow.asSharedFlow()

    fun onFullNameChanged(newName: String) {
        _uiState.value = _uiState.value.copy(
            fullNameInput = newName,
            isNameError = false,
            errorMessage = null
        )
    }

    fun onEmailChanged(newEmail: String) {
        _uiState.value = _uiState.value.copy(
            emailInput = newEmail,
            isEmailError = false,
            errorMessage = null
        )
    }

    fun onPasswordChanged(newPassword: String) {
        val confirmPass = _uiState.value.confirmPasswordInput
        val isMismatch = confirmPass.isNotEmpty() && newPassword != confirmPass

        _uiState.value = _uiState.value.copy(
            passwordInput = newPassword,
            isPasswordError = false,
            isConfirmPasswordError = isMismatch,
            errorMessage = if (isMismatch) "Passwords do not match." else null
        )
    }

    fun onConfirmPasswordChanged(newConfirmPassword: String) {
        val pass = _uiState.value.passwordInput
        val isMismatch = newConfirmPassword.isNotEmpty() && pass != newConfirmPassword

        _uiState.value = _uiState.value.copy(
            confirmPasswordInput = newConfirmPassword,
            isConfirmPasswordError = isMismatch,
            errorMessage = if (isMismatch) "Passwords do not match." else null
        )
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isPasswordVisible = !_uiState.value.isPasswordVisible
        )
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isConfirmPasswordVisible = !_uiState.value.isConfirmPasswordVisible
        )
    }

    fun performSignup() {
        val fullName = _uiState.value.fullNameInput.trim()
        val email = _uiState.value.emailInput.trim()
        val password = _uiState.value.passwordInput
        val confirmPassword = _uiState.value.confirmPasswordInput

        if (fullName.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                isNameError = true,
                errorMessage = "Please enter your full name."
            )
            return
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = _uiState.value.copy(
                isEmailError = true,
                errorMessage = "Please enter a valid email address."
            )
            return
        }

        if (password.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                isPasswordError = true,
                errorMessage = "Please enter a password."
            )
            return
        }

        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(
                isPasswordError = true,
                errorMessage = "Password must be at least 6 characters long."
            )
            return
        }

        if (password != confirmPassword) {
            _uiState.value = _uiState.value.copy(
                isConfirmPasswordError = true,
                errorMessage = "Passwords do not match."
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {
            val result = authRepository.signUp(fullName, email, password)
            if (result.isSuccess) {
                val isSessionActive = result.getOrDefault(false)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true
                )
                if (isSessionActive) {
                    _eventFlow.emit(SignupEvent.NavigateToDashboard)
                } else {
                    _eventFlow.emit(
                        SignupEvent.ShowSuccessAndNavigateToLogin(
                            "✓ Verification Email Sent\nPlease check your inbox to verify your account."
                        )
                    )
                }
            } else {
                val errorMsg = result.exceptionOrNull()?.message
                    ?: "An error occurred during account creation. Please try again."
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = errorMsg
                )
            }
        }
    }

    fun onLoginClicked() {
        viewModelScope.launch {
            _eventFlow.emit(SignupEvent.NavigateToLogin)
        }
    }

    fun performGoogleLoginWithToken(idToken: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {
            Log.d("GoogleDebug", "[GoogleDebug] Google authentication result starting")
            val result = authRepository.signInWithGoogleIdToken(idToken)
            if (result.isSuccess) {
                Log.d("GoogleDebug", "[GoogleDebug] SignupViewModel success state")
                _uiState.value = _uiState.value.copy(
                    isLoading = false
                )
                Log.d("GoogleDebug", "[GoogleDebug] Navigation event emitted")
                _eventFlow.emit(SignupEvent.NavigateToDashboard)
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
