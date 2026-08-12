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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import com.heallens.android.ui.components.ErrorBanner
import com.heallens.android.ui.components.GlassTextField
import com.heallens.android.ui.components.GoogleSignInButton
import com.heallens.android.ui.components.GradientButton
import com.heallens.android.ui.components.PasswordStrengthMeter
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
fun SignupScreen(
    viewModel: SignupViewModel,
    onNavigateToVerification: (String) -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToLogin: () -> Unit
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
                    val idToken = account.idToken
                    Log.d("GoogleDebug", "[GoogleDebug] Account selected")
                    Log.d("GoogleDebug", "[GoogleDebug] ID token present = ${!idToken.isNullOrEmpty()}")
                    if (!idToken.isNullOrEmpty()) {
                        viewModel.performGoogleLoginWithToken(idToken)
                    } else {
                        val errorMsg = "Google Sign-In failed: ID Token is null. Ensure Google Web Client ID is configured in Constants.kt."
                        Log.e("GoogleDebug", "[GoogleDebug] ERROR: $errorMsg")
                        viewModel.onGoogleSignInError(errorMsg)
                    }
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
            Log.d("GoogleDebug", "[GoogleDebug] Navigation event received by SignupScreen: $event")
            when (event) {
                is SignupEvent.NavigateToVerification -> onNavigateToVerification(event.email)
                is SignupEvent.ShowSuccessAndNavigateToLogin -> {
                    android.widget.Toast.makeText(context, event.message, android.widget.Toast.LENGTH_LONG).show()
                    onNavigateToLogin()
                }
                is SignupEvent.NavigateToDashboard -> {
                    Log.d("GoogleDebug", "[GoogleDebug] MainShell navigation triggered")
                    onNavigateToDashboard()
                }
                is SignupEvent.NavigateToLogin -> onNavigateToLogin()
            }
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
            BrandingLogo(size = 72.dp)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = Constants.APP_NAME,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp,
                    brush = Brush.horizontalGradient(listOf(CyanPrimary, PurpleAccent))
                )
            )

            Text(
                text = "Create an account to manage your health records",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Glassmorphic Signup Form Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphicCard(cornerRadius = 24.dp)
                    .padding(24.dp)
            ) {
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Text(
                    text = "Enter your personal details to get started",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextMuted,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )

                // Error Message Banner
                uiState.errorMessage?.let { error ->
                    ErrorBanner(errorMessage = error)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Full Name Input Field
                GlassTextField(
                    value = uiState.fullNameInput,
                    onValueChange = { viewModel.onFullNameChanged(it) },
                    label = "Full Name",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Name Icon",
                            tint = CyanPrimary
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    isError = uiState.isNameError
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email Address Input Field
                GlassTextField(
                    value = uiState.emailInput,
                    onValueChange = { viewModel.onEmailChanged(it) },
                    label = "Email Address",
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
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    isError = uiState.isPasswordError
                )

                // Password Strength Indicator
                PasswordStrengthMeter(password = uiState.passwordInput)

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm Password Input Field
                GlassTextField(
                    value = uiState.confirmPasswordInput,
                    onValueChange = { viewModel.onConfirmPasswordChanged(it) },
                    label = "Confirm Password",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Confirm Password Icon",
                            tint = CyanPrimary
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { viewModel.toggleConfirmPasswordVisibility() }) {
                            Icon(
                                imageVector = if (uiState.isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Confirm Password Visibility",
                                tint = TextMuted
                            )
                        }
                    },
                    visualTransformation = if (uiState.isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            viewModel.performSignup()
                        }
                    ),
                    isError = uiState.isConfirmPasswordError
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Create Account Button
                GradientButton(
                    text = "Create Account →",
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.performSignup()
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

                // Sign In Link Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account?",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = CyanPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        modifier = Modifier.clickable { viewModel.onLoginClicked() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
