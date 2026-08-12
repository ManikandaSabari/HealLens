package com.heallens.android.ui.auth

import androidx.lifecycle.viewModelScope
import com.heallens.android.data.repository.AuthRepository
import com.heallens.android.data.repository.AuthRepositoryImpl
import com.heallens.android.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ResetPasswordViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl()
) : BaseViewModel<ResetPasswordUiState>(ResetPasswordUiState()) {

    private val _eventFlow = MutableSharedFlow<ResetPasswordEvent>()
    val eventFlow: SharedFlow<ResetPasswordEvent> = _eventFlow.asSharedFlow()

    fun onNewPasswordChanged(newPassword: String) {
        val confirmPass = _uiState.value.confirmPasswordInput
        val isMismatch = confirmPass.isNotEmpty() && newPassword != confirmPass

        _uiState.value = _uiState.value.copy(
            newPasswordInput = newPassword,
            isNewPasswordError = false,
            isConfirmPasswordError = isMismatch,
            errorMessage = if (isMismatch) "Passwords do not match." else null
        )
    }

    fun onConfirmPasswordChanged(newConfirmPassword: String) {
        val pass = _uiState.value.newPasswordInput
        val isMismatch = newConfirmPassword.isNotEmpty() && pass != newConfirmPassword

        _uiState.value = _uiState.value.copy(
            confirmPasswordInput = newConfirmPassword,
            isConfirmPasswordError = isMismatch,
            errorMessage = if (isMismatch) "Passwords do not match." else null
        )
    }

    fun toggleNewPasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isNewPasswordVisible = !_uiState.value.isNewPasswordVisible
        )
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isConfirmPasswordVisible = !_uiState.value.isConfirmPasswordVisible
        )
    }

    fun performPasswordReset() {
        val newPassword = _uiState.value.newPasswordInput
        val confirmPassword = _uiState.value.confirmPasswordInput

        if (newPassword.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                isNewPasswordError = true,
                errorMessage = "Please enter a new password."
            )
            return
        }

        if (newPassword.length < 6) {
            _uiState.value = _uiState.value.copy(
                isNewPasswordError = true,
                errorMessage = "Password must be at least 6 characters long."
            )
            return
        }

        if (newPassword != confirmPassword) {
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
            val result = authRepository.updatePassword(newPassword)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true
                )
                _eventFlow.emit(ResetPasswordEvent.NavigateToLogin)
            } else {
                val errorMsg = result.exceptionOrNull()?.message
                    ?: "Failed to update password. Please try again or request a new recovery link."
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = errorMsg
                )
            }
        }
    }

    fun onBackToLoginClicked() {
        viewModelScope.launch {
            _eventFlow.emit(ResetPasswordEvent.NavigateToLogin)
        }
    }
}
