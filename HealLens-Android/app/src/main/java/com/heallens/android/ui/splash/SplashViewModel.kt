package com.heallens.android.ui.splash

import androidx.lifecycle.viewModelScope
import com.heallens.android.navigation.Screen
import com.heallens.android.utils.Constants
import com.heallens.android.viewmodel.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

data class SplashUiState(
    val isLoading: Boolean = true,
    val statusText: String = "Initializing Clinical Engine..."
)

sealed class SplashEvent {
    data class NavigateTo(val route: String) : SplashEvent()
}

class SplashViewModel : BaseViewModel<SplashUiState>(SplashUiState()) {

    private val _eventFlow = MutableSharedFlow<SplashEvent>()
    val eventFlow: SharedFlow<SplashEvent> = _eventFlow.asSharedFlow()

    init {
        startInitialization()
    }

    private fun startInitialization() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                statusText = "Loading Security & Auth Protocols..."
            )
            delay(Constants.SPLASH_DELAY_MS / 2)

            _uiState.value = _uiState.value.copy(
                statusText = "Ready"
            )
            delay(Constants.SPLASH_DELAY_MS / 2)

            // Phase 1: Always navigate to Login route initially
            _eventFlow.emit(SplashEvent.NavigateTo(Screen.Login.route))
        }
    }
}
