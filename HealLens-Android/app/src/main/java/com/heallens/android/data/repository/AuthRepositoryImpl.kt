package com.heallens.android.data.repository

import android.util.Log
import com.heallens.android.data.local.DataStoreManager
import com.heallens.android.data.remote.SupabaseAuthService
import com.heallens.android.model.UserSession

class AuthRepositoryImpl(
    private val supabaseAuthService: SupabaseAuthService = SupabaseAuthService(),
    private val dataStoreManager: DataStoreManager? = null
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<UserSession> {
        return try {
            val session = supabaseAuthService.signInWithEmail(email, password)
            dataStoreManager?.saveSession(
                userId = session.userId,
                email = session.email,
                accessToken = session.accessToken,
                refreshToken = session.refreshToken
            )
            Result.success(session)
        } catch (e: Exception) {
            // Reference Fallback Check (login.html lines 993-1001)
            if (email.trim().equals("admin@heallens.com", ignoreCase = true) && password.trim() == "admin123") {
                val demoSession = UserSession(
                    userId = "demo-admin-id-001",
                    email = "admin@heallens.com",
                    accessToken = "demo_access_token_admin",
                    refreshToken = "demo_refresh_token_admin"
                )
                dataStoreManager?.saveSession(
                    userId = demoSession.userId,
                    email = demoSession.email,
                    accessToken = demoSession.accessToken,
                    refreshToken = demoSession.refreshToken
                )
                Result.success(demoSession)
            } else {
                Result.failure(sanitizeAuthError(e))
            }
        }
    }

    override suspend fun signUp(fullName: String, email: String, password: String): Result<Boolean> {
        return try {
            val isSessionEstablished = supabaseAuthService.signUpWithEmail(fullName, email, password)
            Result.success(isSessionEstablished)
        } catch (e: Exception) {
            Result.failure(sanitizeAuthError(e))
        }
    }

    override suspend fun resendVerification(email: String): Result<Unit> {
        return try {
            supabaseAuthService.resendVerificationEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(sanitizeAuthError(e))
        }
    }

    override suspend fun verifyOtp(email: String, code: String): Result<UserSession> {
        return try {
            val session = supabaseAuthService.verifyEmailOtp(email, code)
            dataStoreManager?.saveSession(
                userId = session.userId,
                email = session.email,
                accessToken = session.accessToken,
                refreshToken = session.refreshToken
            )
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(sanitizeAuthError(e))
        }
    }

    override suspend fun requestPasswordReset(email: String): Result<Unit> {
        return try {
            supabaseAuthService.sendPasswordResetEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(sanitizeAuthError(e))
        }
    }

    override suspend fun setRecoverySession(accessToken: String, refreshToken: String): Result<UserSession> {
        return try {
            val session = supabaseAuthService.setRecoverySession(accessToken, refreshToken)
            dataStoreManager?.saveSession(
                userId = session.userId,
                email = session.email,
                accessToken = session.accessToken,
                refreshToken = session.refreshToken
            )
            if (session.userId.isNotEmpty()) {
                com.heallens.android.data.repository.ClinicalHistoryRepository.setCurrentUser(session.userId)
            }
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePassword(newPassword: String): Result<Unit> {
        return try {
            supabaseAuthService.updateUserPassword(newPassword)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(sanitizeAuthError(e))
        }
    }

    override suspend fun signInWithGoogle(): Result<Unit> {
        return try {
            supabaseAuthService.signInWithGoogle()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogleIdToken(idToken: String): Result<UserSession> {
        return signInWithGoogleAccount(idToken, "", null)
    }

    override suspend fun signInWithGoogleAccount(idToken: String?, email: String?, googleId: String?): Result<UserSession> {
        return try {
            val session = supabaseAuthService.signInWithGoogleAccount(idToken, email ?: "", googleId)
            dataStoreManager?.saveSession(
                userId = session.userId,
                email = session.email,
                accessToken = session.accessToken,
                refreshToken = session.refreshToken
            )
            Log.d("GoogleDebug", "[GoogleDebug] DataStore saveSession started")
            Log.d("GoogleDebug", "[GoogleDebug] DataStore saveSession completed")
            Log.d("GoogleDebug", "[GoogleDebug] DataStore userId exists = ${session.userId.isNotBlank()}")
            Result.success(session)
        } catch (e: Exception) {
            Log.e("GoogleDebug", "[GoogleDebug] ERROR: ${e.message}")
            Result.failure(sanitizeAuthError(e))
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            supabaseAuthService.signOut()
            dataStoreManager?.clearSession()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun sanitizeAuthError(e: Exception): Exception {
        val raw = e.message ?: ""
        val cleanMessage = when {
            raw.contains("UnknownHostException", ignoreCase = true) || raw.contains("ConnectException", ignoreCase = true) || raw.contains("Unable to resolve host", ignoreCase = true) || raw.contains("No address associated with hostname", ignoreCase = true) || raw.contains("NetworkError", ignoreCase = true) ->
                "Network error: Unable to connect to HealLens server. Please check your internet connection."
            raw.contains("Invalid login credentials", ignoreCase = true) || raw.contains("invalid_credentials", ignoreCase = true) || raw.contains("grant_type", ignoreCase = true) ->
                "Invalid email or password. Please check your credentials and try again."
            raw.contains("Email not confirmed", ignoreCase = true) || raw.contains("email_not_confirmed", ignoreCase = true) ->
                "Email address not confirmed. Please check your inbox and verify your account."
            raw.contains("rate limit", ignoreCase = true) || raw.contains("429", ignoreCase = true) ->
                "Email rate limit exceeded. Please wait 60 seconds and try again."
            else -> raw.ifEmpty { "Authentication failed. Please check your details and try again." }
        }
        return Exception(cleanMessage)
    }
}

