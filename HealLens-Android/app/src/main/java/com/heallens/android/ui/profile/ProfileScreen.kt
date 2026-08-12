package com.heallens.android.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import com.heallens.android.ui.components.PasswordStrengthMeter
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heallens.android.ui.components.ErrorBanner
import com.heallens.android.ui.components.GlassTextField
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val genderOptions = listOf("Self", "Father", "Mother", "Spouse", "Child")
    val bloodGroupOptions = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // User Account Badge Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphicCard(cornerRadius = 20.dp)
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(CyanPrimary.copy(alpha = 0.15f))
                            .border(1.5.dp, CyanPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Avatar",
                            tint = CyanPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = uiState.fullNameInput.ifEmpty { "Clinical User" },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )

                        Text(
                            text = uiState.email.ifEmpty { "user@heallens.com" },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .background(PurpleAccent.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                    .border(1.dp, PurpleAccent.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = uiState.provider.uppercase(),
                                    color = PurpleAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified",
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "VERIFIED",
                                        color = Color(0xFF10B981),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Preferred Language Selection Card
            val context = androidx.compose.ui.platform.LocalContext.current
            val activeLanguage by com.heallens.android.utils.LanguageManager.currentLanguageFlow.collectAsState()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphicCard(cornerRadius = 20.dp)
                    .padding(20.dp)
            ) {
                Text(
                    text = com.heallens.android.utils.AppStrings.get("profile_language_title", activeLanguage),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Text(
                    text = com.heallens.android.utils.AppStrings.get("profile_language_subtitle", activeLanguage),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMuted,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    com.heallens.android.utils.AppLanguage.values().forEach { lang ->
                        val isSelected = activeLanguage == lang
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) CyanPrimary.copy(alpha = 0.25f) else SurfaceGlass)
                                .border(
                                    width = 1.5.dp,
                                    color = if (isSelected) CyanPrimary else SurfaceGlassBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    com.heallens.android.utils.LanguageManager.setLanguage(lang, context)
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = lang.flag, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${lang.nativeName} (${lang.displayName})",
                                    color = if (isSelected) CyanPrimary else TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Patient Medical Profile Editing Form Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphicCard(cornerRadius = 20.dp)
                    .padding(20.dp)
            ) {
                Text(
                    text = com.heallens.android.utils.AppStrings.get("profile_patient_title", activeLanguage),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Text(
                    text = "Configure patient attributes for accurate diagnostic risk assessment",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMuted,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                )

                // Error Banner
                uiState.errorMessage?.let { error ->
                    ErrorBanner(errorMessage = error)
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Success Banner
                uiState.successMessage?.let { success ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "✓ $success",
                            color = Color(0xFF10B981),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Full Name
                GlassTextField(
                    value = uiState.fullNameInput,
                    onValueChange = { viewModel.onFullNameChanged(it) },
                    label = "Patient Full Name",
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = CyanPrimary)
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    // Age Input
                    Box(modifier = Modifier.weight(1f)) {
                        GlassTextField(
                            value = uiState.ageInput,
                            onValueChange = { viewModel.onAgeChanged(it) },
                            label = "Age",
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = CyanPrimary)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Patient Type / Gender Selector Chips
                Text(
                    text = "Patient Relation / Type",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    genderOptions.forEach { option ->
                        val isSelected = uiState.genderInput == option
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) CyanPrimary.copy(alpha = 0.2f) else SurfaceGlass)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) CyanPrimary else SurfaceGlassBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.onGenderChanged(option) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = option,
                                color = if (isSelected) CyanPrimary else TextMuted,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Blood Group Selector Chips
                Text(
                    text = "Blood Group",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    bloodGroupOptions.forEach { bg ->
                        val isSelected = uiState.bloodGroupInput == bg
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) PurpleAccent.copy(alpha = 0.25f) else SurfaceGlass)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) PurpleAccent else SurfaceGlassBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.onBloodGroupChanged(bg) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = bg,
                                color = if (isSelected) PurpleAccent else TextMuted,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Emergency Contact Number Input
                GlassTextField(
                    value = uiState.emergencyContactInput,
                    onValueChange = { viewModel.onEmergencyContactChanged(it) },
                    label = "Emergency Helpline Contact Number",
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = CyanPrimary)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Pre-existing Medical Conditions / Notes Input
                GlassTextField(
                    value = uiState.conditionsInput,
                    onValueChange = { viewModel.onConditionsChanged(it) },
                    label = "Pre-existing Health Conditions (e.g. Asthma, Diabetes)",
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.MedicalServices, contentDescription = null, tint = CyanPrimary)
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    singleLine = false
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Save Profile Button
                GradientButton(
                    text = "Save Patient Profile →",
                    onClick = { viewModel.saveProfile() },
                    isLoading = uiState.isSaving
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Change Password Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphicCard(cornerRadius = 20.dp)
                    .padding(20.dp)
            ) {
                Text(
                    text = "Security & Change Password",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Text(
                    text = "Update your credentials for continuous clinical session protection",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMuted,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                )

                uiState.passwordErrorMessage?.let { error ->
                    ErrorBanner(errorMessage = error)
                    Spacer(modifier = Modifier.height(14.dp))
                }

                uiState.passwordSuccessMessage?.let { success ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "✓ $success",
                            color = Color(0xFF10B981),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                GlassTextField(
                    value = uiState.newPasswordInput,
                    onValueChange = { viewModel.onNewPasswordChanged(it) },
                    label = "New Password",
                    visualTransformation = PasswordVisualTransformation(),
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = CyanPrimary)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next)
                )

                if (uiState.newPasswordInput.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    PasswordStrengthMeter(password = uiState.newPasswordInput)
                }

                Spacer(modifier = Modifier.height(14.dp))

                GlassTextField(
                    value = uiState.confirmPasswordInput,
                    onValueChange = { viewModel.onConfirmPasswordChanged(it) },
                    label = "Confirm New Password",
                    visualTransformation = PasswordVisualTransformation(),
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = CyanPrimary)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
                )

                Spacer(modifier = Modifier.height(24.dp))

                GradientButton(
                    text = "Update Security Password →",
                    onClick = { viewModel.changePassword() },
                    isLoading = uiState.isChangingPassword
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
