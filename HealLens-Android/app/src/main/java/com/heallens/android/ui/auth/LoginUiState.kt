package com.heallens.android.ui.auth

data class LoginUiState(
    val emailInput: String = "",
    val passwordInput: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEmailError: Boolean = false,
    val isPasswordError: Boolean = false,
    val isSuccess: Boolean = false
)

sealed class LoginEvent {
    object NavigateToDashboard : LoginEvent()
    object NavigateToSignup : LoginEvent()
    object NavigateToForgotPassword : LoginEvent()
}
