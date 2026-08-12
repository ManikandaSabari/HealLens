package com.heallens.android.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.heallens.android.data.local.DataStoreManager
import com.heallens.android.data.repository.AuthRepository
import com.heallens.android.data.repository.AuthRepositoryImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VerificationViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val dataStoreManager = DataStoreManager(application)
    private val authRepository: AuthRepository = AuthRepositoryImpl(dataStoreManager = dataStoreManager)

    private val _uiState = MutableStateFlow(VerificationUiState())
    val uiState: StateFlow<VerificationUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<VerificationEvent>()
    val eventFlow: SharedFlow<VerificationEvent> = _eventFlow.asSharedFlow()

    private var cooldownJob: Job? = null

    fun setEmail(email: String) {
        if (_uiState.value.email != email) {
            _uiState.value = _uiState.value.copy(email = email)
            startResendCooldown(60)
        }
    }

    fun onOtpCodeChanged(newCode: String) {
        val filtered = newCode.filter { it.isDigit() }.take(6)
        _uiState.value = _uiState.value.copy(
            otpCode = filtered,
            errorMessage = null
        )

        // Automatically trigger verification when 6 digits are typed
        if (filtered.length == 6 && !_uiState.value.isLoading) {
            verifyOtp(filtered)
        }
    }

    fun verifyOtp(codeToVerify: String = _uiState.value.otpCode) {
        val email = _uiState.value.email.trim()
        val code = codeToVerify.trim()

        if (code.length < 6) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please enter the complete 6-digit verification code."
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null,
            successMessage = null
        )

        viewModelScope.launch {
            val result = authRepository.verifyOtp(email, code)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    successMessage = "Email verified successfully!"
                )
                _eventFlow.emit(VerificationEvent.NavigateToDashboard)
            } else {
                val errorMsg = result.exceptionOrNull()?.message
                    ?: "Invalid verification code. Please check your email and try again."
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = errorMsg
                )
            }
        }
    }

    fun resendVerificationEmail() {
        val email = _uiState.value.email.trim()
        if (email.isEmpty() || _uiState.value.resendCooldownSeconds > 0 || _uiState.value.isResending) return

        _uiState.value = _uiState.value.copy(
            isResending = true,
            errorMessage = null,
            successMessage = null
        )

        viewModelScope.launch {
            val result = authRepository.resendVerification(email)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isResending = false,
                    successMessage = "Verification link/code resent! Please check your inbox."
                )
                startResendCooldown(60)
            } else {
                val errorMsg = result.exceptionOrNull()?.message
                    ?: "Failed to resend verification email. Please try again later."
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
            _eventFlow.emit(VerificationEvent.NavigateToLogin)
        }
    }

    override fun onCleared() {
        super.onCleared()
        cooldownJob?.cancel()
    }
}
