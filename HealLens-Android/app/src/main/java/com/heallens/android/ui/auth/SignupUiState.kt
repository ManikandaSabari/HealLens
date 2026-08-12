package com.heallens.android.ui.auth

data class SignupUiState(
    val fullNameInput: String = "",
    val emailInput: String = "",
    val passwordInput: String = "",
    val confirmPasswordInput: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isNameError: Boolean = false,
    val isEmailError: Boolean = false,
    val isPasswordError: Boolean = false,
    val isConfirmPasswordError: Boolean = false,
    val isSuccess: Boolean = false
)

sealed class SignupEvent {
    data class NavigateToVerification(val email: String) : SignupEvent()
    data class ShowSuccessAndNavigateToLogin(val message: String) : SignupEvent()
    object NavigateToDashboard : SignupEvent()
    object NavigateToLogin : SignupEvent()
}
