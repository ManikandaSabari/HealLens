package com.heallens.android.data.repository

import com.heallens.android.model.UserSession

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<UserSession>
    suspend fun signUp(fullName: String, email: String, password: String): Result<Boolean>
    suspend fun resendVerification(email: String): Result<Unit>
    suspend fun verifyOtp(email: String, code: String): Result<UserSession>
    suspend fun requestPasswordReset(email: String): Result<Unit>
    suspend fun setRecoverySession(accessToken: String, refreshToken: String): Result<UserSession>
    suspend fun updatePassword(newPassword: String): Result<Unit>
    suspend fun signInWithGoogle(): Result<Unit>
    suspend fun signInWithGoogleIdToken(idToken: String): Result<UserSession>
    suspend fun signInWithGoogleAccount(idToken: String?, email: String?, googleId: String?): Result<UserSession>
    suspend fun logout(): Result<Unit>
}

