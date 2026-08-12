package com.heallens.android.ui.auth

data class ResetPasswordUiState(
    val newPasswordInput: String = "",
    val confirmPasswordInput: String = "",
    val isNewPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isNewPasswordError: Boolean = false,
    val isConfirmPasswordError: Boolean = false,
    val isSuccess: Boolean = false
)

sealed class ResetPasswordEvent {
    object NavigateToLogin : ResetPasswordEvent()
    object NavigateToDashboard : ResetPasswordEvent()
}
