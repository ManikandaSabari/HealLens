package com.heallens.android.ui.profile

data class ProfileUiState(
    val email: String = "",
    val provider: String = "Email",
    val isEmailVerified: Boolean = true,
    val fullNameInput: String = "",
    val ageInput: String = "",
    val genderInput: String = "Self",
    val bloodGroupInput: String = "O+",
    val emergencyContactInput: String = "",
    val conditionsInput: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val newPasswordInput: String = "",
    val confirmPasswordInput: String = "",
    val isChangingPassword: Boolean = false,
    val passwordSuccessMessage: String? = null,
    val passwordErrorMessage: String? = null
)

sealed class ProfileEvent {
    object NavigateToLogin : ProfileEvent()
}
