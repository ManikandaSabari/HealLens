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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heallens.android.ui.components.BrandingLogo
import com.heallens.android.ui.components.ErrorBanner
import com.heallens.android.ui.components.GradientButton
import com.heallens.android.ui.theme.CyanPrimary
import com.heallens.android.ui.theme.DarkBackground
import com.heallens.android.ui.theme.PurpleAccent
import com.heallens.android.ui.theme.SurfaceGlass
import com.heallens.android.ui.theme.SurfaceGlassBorder
import com.heallens.android.ui.theme.TextMuted
import com.heallens.android.ui.theme.TextPrimary
import com.heallens.android.ui.theme.TextSecondary
import com.heallens.android.ui.theme.glassmorphicCard
import com.heallens.android.utils.Constants
import kotlinx.coroutines.flow.collectLatest

@Composable
fun VerificationScreen(
    email: String,
    viewModel: VerificationViewModel,
    onNavigateToDashboard: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(email) {
        viewModel.setEmail(email)
    }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is VerificationEvent.NavigateToDashboard -> onNavigateToDashboard()
                is VerificationEvent.NavigateToLogin -> onNavigateToLogin()
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
                text = "Account Verification Required",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Glassmorphic Verification Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphicCard(cornerRadius = 24.dp)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Verify Email Address",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "We sent an email verification link & 6-digit code to:",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                )

                Text(
                    text = uiState.email.ifEmpty { email },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = CyanPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Error Message Banner
                uiState.errorMessage?.let { error ->
                    ErrorBanner(errorMessage = error)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Success Message Banner
                uiState.successMessage?.let { success ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = androidx.compose.ui.graphics.Color(0xFF10B981).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = androidx.compose.ui.graphics.Color(0xFF10B981).copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "✓ $success",
                            color = androidx.compose.ui.graphics.Color(0xFF10B981),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 6-Digit OTP Code Input Box Grid
                OtpCodeInputGrid(
                    otpCode = uiState.otpCode,
                    onOtpCodeChanged = { viewModel.onOtpCodeChanged(it) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Verify Button
                GradientButton(
                    text = "Verify Account →",
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.verifyOtp()
                    },
                    isLoading = uiState.isLoading
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Resend Cooldown Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.resendCooldownSeconds > 0) {
                        Text(
                            text = "Resend code in ${uiState.resendCooldownSeconds}s",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        )
                    } else {
                        Text(
                            text = if (uiState.isResending) "Resending email..." else "Didn't receive code?",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        if (!uiState.isResending) {
                            Text(
                                text = "Resend Code",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = CyanPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                modifier = Modifier.clickable { viewModel.resendVerificationEmail() }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Back to Login Link
                Text(
                    text = "← Back to Sign In",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.clickable { viewModel.onBackToLoginClicked() }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun OtpCodeInputGrid(
    otpCode: String,
    onOtpCodeChanged: (String) -> Unit
) {
    BasicTextField(
        value = otpCode,
        onValueChange = { newValue ->
            if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
                onOtpCodeChanged(newValue)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                for (index in 0 until 6) {
                    val char = if (index < otpCode.length) otpCode[index].toString() else ""
                    val isFocused = index == otpCode.length || (index == 5 && otpCode.length == 6)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .background(SurfaceGlass, RoundedCornerShape(12.dp))
                            .border(
                                width = 1.dp,
                                color = if (isFocused) CyanPrimary else SurfaceGlassBorder,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }
        }
    )
}
