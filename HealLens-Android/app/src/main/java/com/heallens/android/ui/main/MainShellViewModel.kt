package com.heallens.android.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.heallens.android.data.local.DataStoreManager
import com.heallens.android.data.repository.AuthRepository
import com.heallens.android.data.repository.AuthRepositoryImpl
import com.heallens.android.data.repository.ClinicalHistoryRepository
import com.heallens.android.data.repository.EmergencyContactRepository
import com.heallens.android.ui.components.NavDestination
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

import com.heallens.android.data.remote.SupabaseAuthService
import kotlinx.coroutines.flow.firstOrNull

class MainShellViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val dataStoreManager = DataStoreManager(application)
    private val authRepository: AuthRepository = AuthRepositoryImpl(dataStoreManager = dataStoreManager)
    private val supabaseAuthService by lazy { SupabaseAuthService() }

    private val _uiState = MutableStateFlow(MainShellUiState())
    val uiState: StateFlow<MainShellUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<MainShellEvent>()
    val eventFlow: SharedFlow<MainShellEvent> = _eventFlow.asSharedFlow()

    init {
        observeUserSession()
    }

    private fun observeUserSession() {
        viewModelScope.launch {
            dataStoreManager.userIdFlow.collectLatest { userId ->
                if (!userId.isNullOrEmpty()) {
                    _uiState.value = _uiState.value.copy(userId = userId)

                    val token = dataStoreManager.accessTokenFlow.firstOrNull()
                    val refresh = dataStoreManager.refreshTokenFlow.firstOrNull()
                    if (!token.isNullOrEmpty() && !refresh.isNullOrEmpty()) {
                        supabaseAuthService.restoreSession(token, refresh)
                    }

                    ClinicalHistoryRepository.setCurrentUser(userId)
                    EmergencyContactRepository.setCurrentUser(userId)
                } else {
                    ClinicalHistoryRepository.clearCurrentSession()
                    EmergencyContactRepository.clearCurrentSession()
                }
            }
        }
    }

    fun selectDestination(destination: NavDestination) {
        _uiState.value = _uiState.value.copy(currentDestination = destination)
    }

    fun showLogoutDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showLogoutDialog = show)
    }

    fun performLogout() {
        _uiState.value = _uiState.value.copy(isLoggingOut = true, showLogoutDialog = false)
        viewModelScope.launch {
            authRepository.logout()
            ClinicalHistoryRepository.clearCurrentSession()
            EmergencyContactRepository.clearCurrentSession()
            _uiState.value = _uiState.value.copy(isLoggingOut = false, isLoggedOut = true)
            _eventFlow.emit(MainShellEvent.NavigateToLogin)
        }
    }
}
