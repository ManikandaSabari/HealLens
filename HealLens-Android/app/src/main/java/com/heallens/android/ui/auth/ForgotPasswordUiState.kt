package com.heallens.android.ui.auth

data class ForgotPasswordUiState(
    val emailInput: String = "",
    val isLoading: Boolean = false,
    val isResending: Boolean = false,
    val resendCooldownSeconds: Int = 0,
    val isEmailSent: Boolean = false,
    val errorMessage: String? = null,
    val isEmailError: Boolean = false
)

sealed class ForgotPasswordEvent {
    object NavigateToLogin : ForgotPasswordEvent()
}
