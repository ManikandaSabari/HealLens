package com.heallens.android.ui.auth

import android.util.Patterns
import androidx.lifecycle.viewModelScope
import com.heallens.android.data.repository.AuthRepository
import com.heallens.android.data.repository.AuthRepositoryImpl
import com.heallens.android.viewmodel.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl()
) : BaseViewModel<ForgotPasswordUiState>(ForgotPasswordUiState()) {

    private val _eventFlow = MutableSharedFlow<ForgotPasswordEvent>()
    val eventFlow: SharedFlow<ForgotPasswordEvent> = _eventFlow.asSharedFlow()

    private var cooldownJob: Job? = null

    fun onEmailChanged(newEmail: String) {
        _uiState.value = _uiState.value.copy(
            emailInput = newEmail,
            isEmailError = false,
            errorMessage = null
        )
    }

    fun requestPasswordReset() {
        val email = _uiState.value.emailInput.trim()

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
                errorMessage = "Please enter a valid email address."
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {
            val result = authRepository.requestPasswordReset(email)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isEmailSent = true
                )
                startResendCooldown(60)
            } else {
                val errorMsg = result.exceptionOrNull()?.message
                    ?: "Unable to send password reset email. Please verify your email and try again."
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = errorMsg
                )
            }
        }
    }

    fun resendPasswordReset() {
        val email = _uiState.value.emailInput.trim()
        if (email.isEmpty() || _uiState.value.resendCooldownSeconds > 0 || _uiState.value.isResending) return

        _uiState.value = _uiState.value.copy(
            isResending = true,
            errorMessage = null
        )

        viewModelScope.launch {
            val result = authRepository.requestPasswordReset(email)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isResending = false)
                startResendCooldown(60)
            } else {
                val errorMsg = result.exceptionOrNull()?.message
                    ?: "Failed to resend reset email. Please try again later."
                _uiState.value = _uiState.value.copy(
                    isResending = false,
                    errorMessage = errorMsg
                )
            }
        }
    }

    private fun startResendCooldown(seconds: Int) {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            for (i in seconds downTo 0) {
                _uiState.value = _uiState.value.copy(resendCooldownSeconds = i)
                delay(1000)
            }
        }
    }

    fun onBackToLoginClicked() {
        viewModelScope.launch {
            _eventFlow.emit(ForgotPasswordEvent.NavigateToLogin)
        }
    }

    override fun onCleared() {
        super.onCleared()
        cooldownJob?.cancel()
    }
}
