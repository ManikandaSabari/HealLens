package com.heallens.android.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.heallens.android.ui.components.BrandingLogo
import com.heallens.android.ui.components.ErrorBanner
import com.heallens.android.ui.components.GlassTextField
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
fun ResetPasswordScreen(
    viewModel: ResetPasswordViewModel,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is ResetPasswordEvent.NavigateToLogin -> onNavigateToLogin()
                is ResetPasswordEvent.NavigateToDashboard -> onNavigateToLogin()
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

            // Branding Logo Header
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
                text = "Set New Password Credentials",
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
                Text(
                    text = "Update Password",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Text(
                    text = "Enter your new password below to update your account access credentials.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )

                // Error Message Banner
                uiState.errorMessage?.let { error ->
                    ErrorBanner(errorMessage = error)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // New Password Input Field
                GlassTextField(
                    value = uiState.newPasswordInput,
                    onValueChange = { viewModel.onNewPasswordChanged(it) },
                    label = "New Password",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock Icon",
                            tint = CyanPrimary
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { viewModel.toggleNewPasswordVisibility() }) {
                            Icon(
                                imageVector = if (uiState.isNewPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Visibility",
                                tint = TextMuted
                            )
                        }
                    },
                    visualTransformation = if (uiState.isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    isError = uiState.isNewPasswordError
                )

                // Password Strength Meter
                PasswordStrengthMeter(password = uiState.newPasswordInput)

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm Password Input Field
                GlassTextField(
                    value = uiState.confirmPasswordInput,
                    onValueChange = { viewModel.onConfirmPasswordChanged(it) },
                    label = "Confirm Password",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Confirm Lock Icon",
                            tint = CyanPrimary
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { viewModel.toggleConfirmPasswordVisibility() }) {
                            Icon(
                                imageVector = if (uiState.isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Visibility",
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
                            viewModel.performPasswordReset()
                        }
                    ),
                    isError = uiState.isConfirmPasswordError
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Update Password Button
                GradientButton(
                    text = "Update Password →",
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.performPasswordReset()
                    },
                    isLoading = uiState.isLoading
                )

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
