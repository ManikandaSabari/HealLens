package com.heallens.android.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heallens.android.ui.components.BrandingLogo
import com.heallens.android.ui.components.ErrorBanner
import com.heallens.android.ui.components.GlassTextField
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
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is ForgotPasswordEvent.NavigateToLogin -> onNavigateToLogin()
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
                text = "Password Reset Recovery",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Glassmorphic Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphicCard(cornerRadius = 24.dp)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!uiState.isEmailSent) {
                    // Form State: Enter Email
                    Text(
                        text = "Reset Password",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )

                    Text(
                        text = "Enter your registered email address to receive password recovery instructions.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextMuted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )

                    // Error Banner
                    uiState.errorMessage?.let { error ->
                        ErrorBanner(errorMessage = error)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Email Input Field
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
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                viewModel.requestPasswordReset()
                            }
                        ),
                        isError = uiState.isEmailError
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Submit Button
                    GradientButton(
                        text = "Send Reset Link →",
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.requestPasswordReset()
                        },
                        isLoading = uiState.isLoading
                    )
                } else {
                    // Confirmation State: Email Sent
                    Text(
                        text = "✓ Reset Email Sent",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = CyanPrimary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = CyanPrimary.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Recovery link sent to:",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                            Text(
                                text = uiState.emailInput,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
                            )
                            Text(
                                text = "Check your email inbox and click the reset link to choose a new password.",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Resend Cooldown Timer Section
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (uiState.resendCooldownSeconds > 0) {
                            Text(
                                text = "Resend available in ${uiState.resendCooldownSeconds}s",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextMuted,
                                    fontSize = 13.sp
                                )
                            )
                        } else {
                            Text(
                                text = if (uiState.isResending) "Resending reset email..." else "Didn't receive the email?",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextMuted,
                                    fontSize = 13.sp
                                )
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            if (!uiState.isResending) {
                                Text(
                                    text = "Resend Email",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = CyanPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    ),
                                    modifier = Modifier.clickable { viewModel.resendPasswordReset() }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Back to Login Link
                Text(
                    text = "← Back to Sign In",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = CyanPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.clickable { viewModel.onBackToLoginClicked() }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
