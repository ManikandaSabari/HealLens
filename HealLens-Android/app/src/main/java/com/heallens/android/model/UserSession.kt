package com.heallens.android.model

data class UserSession(
    val userId: String,
    val email: String,
    val accessToken: String?,
    val refreshToken: String?,
    val isEmailVerified: Boolean = true
)
