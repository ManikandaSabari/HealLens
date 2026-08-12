package com.heallens.android.ui.auth

data class VerificationUiState(
    val email: String = "",
    val otpCode: String = "", // 6 digits
    val isLoading: Boolean = false,
    val isResending: Boolean = false,
    val resendCooldownSeconds: Int = 0,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isSuccess: Boolean = false
)

sealed class VerificationEvent {
    object NavigateToDashboard : VerificationEvent()
    object NavigateToLogin : VerificationEvent()
}
