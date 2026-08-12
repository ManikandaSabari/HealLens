package com.heallens.android.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.heallens.android.ui.components.BrandingLogo
import com.heallens.android.ui.components.DefaultCredentialsPill
import com.heallens.android.ui.components.ErrorBanner
import com.heallens.android.ui.components.GlassTextField
import com.heallens.android.ui.components.GoogleSignInButton
import com.heallens.android.ui.components.GradientButton
import com.heallens.android.ui.theme.CyanPrimary
import com.heallens.android.ui.theme.DarkBackground
import com.heallens.android.ui.theme.PurpleAccent
import com.heallens.android.ui.theme.TextMuted
import com.heallens.android.ui.theme.TextPrimary
import com.heallens.android.ui.theme.TextSecondary
import com.heallens.android.ui.theme.glassmorphicCard
import com.heallens.android.utils.Constants
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToDashboard: () -> Unit,
    onNavigateToSignup: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    val email = account.email
                    val googleId = account.id
                    val idToken = account.idToken
                    Log.d("GoogleDebug", "[GoogleDebug] Google account returned")
                    Log.d("GoogleDebug", "[GoogleDebug] Google email exists = ${!email.isNullOrEmpty()}")
                    Log.d("GoogleDebug", "[GoogleDebug] Google account id exists = ${!googleId.isNullOrEmpty()}")
                    Log.d("GoogleDebug", "[GoogleDebug] Google ID token exists = ${!idToken.isNullOrEmpty()}")
                    viewModel.performGoogleLoginWithAccount(idToken, email, googleId)
                } else {
                    viewModel.onGoogleSignInError("Google Sign-In failed: Google account selection returned no data.")
                }
            } catch (e: ApiException) {
                if (e.statusCode == 12501 || e.statusCode == 12502) {
                    viewModel.onGoogleSignInCancelled()
                } else {
                    val errorMsg = "Google Sign-In failed (Code ${e.statusCode}): ${e.message}"
                    Log.e("GoogleDebug", "[GoogleDebug] ERROR: $errorMsg")
                    viewModel.onGoogleSignInError(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Google Sign-In failed: ${e.message}"
                Log.e("GoogleDebug", "[GoogleDebug] ERROR: $errorMsg")
                viewModel.onGoogleSignInError(errorMsg)
            }
        } else {
            viewModel.onGoogleSignInCancelled()
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            Log.d("GoogleDebug", "[GoogleDebug] Navigation event received by LoginScreen: $event")
            when (event) {
                is LoginEvent.NavigateToDashboard -> {
                    Log.d("GoogleDebug", "[GoogleDebug] MainShell navigation triggered")
                    onNavigateToDashboard()
                }
                is LoginEvent.NavigateToSignup -> onNavigateToSignup()
                is LoginEvent.NavigateToForgotPassword -> onNavigateToForgotPassword()
            }
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            Log.d("GoogleDebug", "[GoogleDebug] MainShell navigation triggered via uiState.isSuccess")
            onNavigateToDashboard()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Branding Header
            BrandingLogo(size = 84.dp)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = Constants.APP_NAME,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 30.sp,
                    brush = Brush.horizontalGradient(listOf(CyanPrimary, PurpleAccent))
                )
            )

            Text(
                text = Constants.APP_TAGLINE,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Glassmorphic Login Form Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphicCard(cornerRadius = 24.dp)
                    .padding(24.dp)
            ) {
                Text(
                    text = "Welcome Back",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Text(
                    text = "Sign in to access clinical diagnostics & history",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextMuted,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )

                // Error Message Banner if any
                uiState.errorMessage?.let { error ->
                    ErrorBanner(errorMessage = error)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Default Credentials Helper Pill
                DefaultCredentialsPill(
                    onFillCredentials = { email, pass ->
                        viewModel.fillDemoCredentials(email, pass)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email Input Field
                GlassTextField(
                    value = uiState.emailInput,
                    onValueChange = { viewModel.onEmailChanged(it) },
                    label = "Email Address",
                    placeholder = "admin@heallens.com",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email Icon",
                            tint = CyanPrimary
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    isError = uiState.isEmailError
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password Input Field
                GlassTextField(
                    value = uiState.passwordInput,
                    onValueChange = { viewModel.onPasswordChanged(it) },
                    label = "Password",
                    placeholder = "••••••••",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password Icon",
                            tint = CyanPrimary
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                            Icon(
                                imageVector = if (uiState.isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Password Visibility",
                                tint = TextMuted
                            )
                        }
                    },
                    visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            viewModel.performLogin()
                        }
                    ),
                    isError = uiState.isPasswordError
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Forgot Password Link
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "Forgot Password?",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = CyanPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier
                            .clickable { viewModel.onForgotPasswordClicked() }
                            .padding(vertical = 4.dp, horizontal = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Login CTA Button
                GradientButton(
                    text = "Login to Account",
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.performLogin()
                    },
                    isLoading = uiState.isLoading
                )

                // Continue with Google OAuth Button
                GoogleSignInButton(
                    onClick = {
                        focusManager.clearFocus()
                        Log.d("GoogleDebug", "[GoogleDebug] Google launcher started")
                        try {
                            val webClientId = Constants.GOOGLE_WEB_CLIENT_ID
                            val gsoBuilder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestEmail()
                            if (webClientId.isNotBlank()) {
                                gsoBuilder.requestIdToken(webClientId)
                            }
                            val client = GoogleSignIn.getClient(context, gsoBuilder.build())
                            client.signOut().addOnCompleteListener {
                                googleSignInLauncher.launch(client.signInIntent)
                            }
                        } catch (e: Exception) {
                            viewModel.onGoogleSignInError("Unable to launch Google Sign-In: ${e.message}")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Signup Link Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Don't have an account?",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Sign Up",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = CyanPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        modifier = Modifier.clickable { viewModel.onSignupClicked() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
