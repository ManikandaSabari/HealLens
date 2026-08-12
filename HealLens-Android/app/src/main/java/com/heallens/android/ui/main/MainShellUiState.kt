package com.heallens.android.ui.main

import com.heallens.android.ui.components.NavDestination

data class MainShellUiState(
    val currentDestination: NavDestination = NavDestination.DASHBOARD,
    val userId: String = "",
    val userEmail: String = "",
    val showLogoutDialog: Boolean = false,
    val isLoggingOut: Boolean = false,
    val isLoggedOut: Boolean = false
)

sealed class MainShellEvent {
    object NavigateToLogin : MainShellEvent()
}
