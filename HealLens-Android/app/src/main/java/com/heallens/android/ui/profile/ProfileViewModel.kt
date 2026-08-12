package com.heallens.android.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.heallens.android.data.local.DataStoreManager
import com.heallens.android.data.repository.AuthRepository
import com.heallens.android.data.repository.AuthRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val dataStoreManager = DataStoreManager(application)
    private val authRepository: AuthRepository = AuthRepositoryImpl(dataStoreManager = dataStoreManager)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            dataStoreManager.userEmailFlow.collectLatest { email ->
                if (!email.isNullOrEmpty()) {
                    _uiState.value = _uiState.value.copy(email = email)
                }
            }
        }

        viewModelScope.launch {
            dataStoreManager.patientProfileFlow.collectLatest { profile ->
                _uiState.value = _uiState.value.copy(
                    fullNameInput = profile["name"] ?: "",
                    ageInput = profile["age"] ?: "",
                    genderInput = profile["gender"] ?: "Self",
                    bloodGroupInput = profile["bloodGroup"] ?: "O+",
                    emergencyContactInput = profile["emergencyContact"] ?: "",
                    conditionsInput = profile["conditions"] ?: ""
                )
            }
        }
    }

    fun onFullNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(fullNameInput = name, successMessage = null, errorMessage = null)
    }

    fun onAgeChanged(age: String) {
        _uiState.value = _uiState.value.copy(ageInput = age.filter { it.isDigit() }, successMessage = null, errorMessage = null)
    }

    fun onGenderChanged(gender: String) {
        _uiState.value = _uiState.value.copy(genderInput = gender, successMessage = null, errorMessage = null)
    }

    fun onBloodGroupChanged(group: String) {
        _uiState.value = _uiState.value.copy(bloodGroupInput = group, successMessage = null, errorMessage = null)
    }

    fun onEmergencyContactChanged(contact: String) {
        _uiState.value = _uiState.value.copy(emergencyContactInput = contact, successMessage = null, errorMessage = null)
    }

    fun onConditionsChanged(conditions: String) {
        _uiState.value = _uiState.value.copy(conditionsInput = conditions, successMessage = null, errorMessage = null)
    }

    fun saveProfile() {
        val name = _uiState.value.fullNameInput.trim()
        val age = _uiState.value.ageInput.trim()
        val gender = _uiState.value.genderInput
        val bloodGroup = _uiState.value.bloodGroupInput
        val contact = _uiState.value.emergencyContactInput.trim()
        val conditions = _uiState.value.conditionsInput.trim()

        if (name.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter full name.")
            return
        }

        _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null, successMessage = null)

        viewModelScope.launch {
            try {
                dataStoreManager.savePatientProfile(
                    name = name,
                    age = age,
                    gender = gender,
                    bloodGroup = bloodGroup,
                    emergencyContact = contact,
                    conditions = conditions
                )
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    successMessage = "Patient profile saved successfully!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "Failed to save profile: ${e.message}"
                )
            }
        }
    }

    fun onNewPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(newPasswordInput = password, passwordSuccessMessage = null, passwordErrorMessage = null)
    }

    fun onConfirmPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(confirmPasswordInput = password, passwordSuccessMessage = null, passwordErrorMessage = null)
    }

    fun changePassword() {
        val newPassword = _uiState.value.newPasswordInput
        val confirmPassword = _uiState.value.confirmPasswordInput

        if (newPassword.length < 6) {
            _uiState.value = _uiState.value.copy(passwordErrorMessage = "New password must be at least 6 characters.")
            return
        }
        if (newPassword != confirmPassword) {
            _uiState.value = _uiState.value.copy(passwordErrorMessage = "Passwords do not match.")
            return
        }

        _uiState.value = _uiState.value.copy(isChangingPassword = true, passwordErrorMessage = null, passwordSuccessMessage = null)
        viewModelScope.launch {
            try {
                authRepository.updatePassword(newPassword)
                _uiState.value = _uiState.value.copy(
                    isChangingPassword = false,
                    newPasswordInput = "",
                    confirmPasswordInput = "",
                    passwordSuccessMessage = "Password updated successfully!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isChangingPassword = false,
                    passwordErrorMessage = e.message ?: "Failed to update password."
                )
            }
        }
    }
}
