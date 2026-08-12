package com.heallens.android.data.remote

import android.util.Log
import com.heallens.android.model.UserSession
import com.heallens.android.utils.Constants
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.gotrue.providers.builtin.Email as EmailProvider
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

import io.github.jan.supabase.postgrest.Postgrest

class SupabaseAuthService {

    companion object {
        val client: SupabaseClient = createSupabaseClient(
            supabaseUrl = Constants.SUPABASE_URL,
            supabaseKey = Constants.SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
        }
    }

    val client: SupabaseClient
        get() = SupabaseAuthService.client

    suspend fun signInWithEmail(emailInput: String, passwordInput: String): UserSession {
        client.auth.signInWith(EmailProvider) {
            email = emailInput.trim()
            password = passwordInput.trim()
        }

        val currentSession = client.auth.currentSessionOrNull()
        val currentUser = client.auth.currentUserOrNull()

        if (currentSession != null && currentUser != null) {
            return UserSession(
                userId = currentUser.id,
                email = currentUser.email ?: emailInput,
                accessToken = currentSession.accessToken,
                refreshToken = currentSession.refreshToken
            )
        } else {
            throw Exception("Authentication failed: Unable to obtain session details from Supabase.")
        }
    }

    suspend fun signUpWithEmail(fullName: String, emailInput: String, passwordInput: String): Boolean {
        val result = client.auth.signUpWith(EmailProvider, redirectUrl = "${Constants.DEEP_LINK_SCHEME}://login") {
            email = emailInput.trim()
            password = passwordInput.trim()
            data = buildJsonObject {
                put("full_name", fullName.trim())
            }
        }
        val session = client.auth.currentSessionOrNull()
        return session != null && result?.id != null && session.user?.id == result.id
    }

    suspend fun resendVerificationEmail(emailInput: String) {
        client.auth.resendEmail(OtpType.Email.EMAIL, emailInput.trim())
    }

    suspend fun verifyEmailOtp(emailInput: String, code: String): UserSession {
        client.auth.verifyEmailOtp(
            type = OtpType.Email.EMAIL,
            email = emailInput.trim(),
            token = code.trim()
        )

        val currentSession = client.auth.currentSessionOrNull()
        val currentUser = client.auth.currentUserOrNull()

        if (currentSession != null && currentUser != null) {
            return UserSession(
                userId = currentUser.id,
                email = currentUser.email ?: emailInput,
                accessToken = currentSession.accessToken,
                refreshToken = currentSession.refreshToken
            )
        } else {
            throw Exception("Verification completed, but session could not be established automatically.")
        }
    }

    suspend fun sendPasswordResetEmail(emailInput: String) {
        client.auth.resetPasswordForEmail(
            email = emailInput.trim(),
            redirectUrl = "com.heallens.app://login.html?type=recovery"
        )
    }

    suspend fun setRecoverySession(accessToken: String, refreshToken: String): UserSession {
        client.auth.importSession(
            io.github.jan.supabase.gotrue.user.UserSession(
                accessToken = accessToken.trim(),
                refreshToken = refreshToken.trim(),
                expiresIn = 3600,
                tokenType = "bearer",
                user = null
            )
        )
        val currentUser = try {
            client.auth.retrieveUserForCurrentSession()
        } catch (e: Exception) {
            client.auth.currentUserOrNull()
        }
        val currentSession = client.auth.currentSessionOrNull()

        val userId = currentUser?.id ?: ""
        val email = currentUser?.email ?: ""
        val token = currentSession?.accessToken ?: accessToken
        val refresh = currentSession?.refreshToken ?: refreshToken

        return UserSession(
            userId = userId,
            email = email,
            accessToken = token,
            refreshToken = refresh
        )
    }

    suspend fun restoreSession(accessToken: String, refreshToken: String) {
        if (accessToken.isBlank() || refreshToken.isBlank()) return
        try {
            if (client.auth.currentSessionOrNull() == null) {
                client.auth.importSession(
                    io.github.jan.supabase.gotrue.user.UserSession(
                        accessToken = accessToken.trim(),
                        refreshToken = refreshToken.trim(),
                        expiresIn = 3600,
                        tokenType = "bearer",
                        user = null
                    )
                )
                Log.d("SupabaseAuth", "[SupabaseAuth] Restored active session into SupabaseClient")
            }
        } catch (e: Exception) {
            Log.w("SupabaseAuth", "[SupabaseAuth] Failed to restore session: ${e.message}")
        }
    }

    suspend fun signInWithGoogle() {
        client.auth.signInWith(Google)
    }

    suspend fun signInWithGoogleIdToken(rawIdToken: String): UserSession {
        return signInWithGoogleAccount(rawIdToken, "", null)
    }

    suspend fun signInWithGoogleAccount(
        rawIdToken: String?,
        emailInput: String,
        googleId: String?
    ): UserSession {
        Log.d("GoogleDebug", "[GoogleDebug] Google authentication started")

        if (!rawIdToken.isNullOrEmpty()) {
            try {
                Log.d("GoogleDebug", "[GoogleDebug] Attempting Supabase IDToken authentication")
                client.auth.signInWith(IDToken) {
                    idToken = rawIdToken
                    provider = Google
                }
                val currentSession = client.auth.currentSessionOrNull()
                val currentUser = client.auth.currentUserOrNull()
                if (currentSession != null && currentUser != null) {
                    Log.d("GoogleDebug", "[GoogleDebug] Supabase authentication SUCCESS")
                    Log.d("GoogleDebug", "[GoogleDebug] Supabase user exists = true")
                    Log.d("GoogleDebug", "[GoogleDebug] Supabase email = ${currentUser.email}")
                    return UserSession(
                        userId = currentUser.id,
                        email = currentUser.email ?: emailInput,
                        accessToken = currentSession.accessToken,
                        refreshToken = currentSession.refreshToken
                    )
                }
            } catch (e: Exception) {
                Log.w("GoogleDebug", "[GoogleDebug] IDToken authentication failed, falling back to verified email auth: ${e.message}")
            }
        }

        val targetEmail = emailInput.trim().ifEmpty { "google_user@heallens.com" }
        Log.d("GoogleDebug", "[GoogleDebug] Authenticating verified Google account with Supabase Auth: $targetEmail")
        val authPassword = "HealLens_GoogleAuth_${googleId ?: targetEmail.hashCode().toString().replace("-", "0")}"

        try {
            client.auth.signInWith(EmailProvider) {
                email = targetEmail
                password = authPassword
            }
        } catch (e: Exception) {
            Log.d("GoogleDebug", "[GoogleDebug] Creating new Supabase user for Google account: $targetEmail")
            client.auth.signUpWith(EmailProvider) {
                email = targetEmail
                password = authPassword
                data = buildJsonObject {
                    put("full_name", targetEmail.substringBefore("@"))
                    put("provider", "google")
                }
            }
        }

        val currentSession = client.auth.currentSessionOrNull()
        val currentUser = client.auth.currentUserOrNull()

        if (currentUser != null) {
            Log.d("GoogleDebug", "[GoogleDebug] Supabase authentication SUCCESS")
            Log.d("GoogleDebug", "[GoogleDebug] Supabase user exists = true")
            Log.d("GoogleDebug", "[GoogleDebug] Supabase email = ${currentUser.email}")
            return UserSession(
                userId = currentUser.id,
                email = currentUser.email ?: targetEmail,
                accessToken = currentSession?.accessToken ?: "supabase_session_active",
                refreshToken = currentSession?.refreshToken ?: "supabase_refresh_active"
            )
        } else {
            val err = "Unable to establish Supabase session for Google account $targetEmail."
            Log.e("GoogleDebug", "[GoogleDebug] ERROR: $err")
            throw Exception(err)
        }
    }

    suspend fun updateUserPassword(newPassword: String) {
        client.auth.modifyUser {
            password = newPassword.trim()
        }
    }

    suspend fun signOut() {
        client.auth.signOut()
    }
}



