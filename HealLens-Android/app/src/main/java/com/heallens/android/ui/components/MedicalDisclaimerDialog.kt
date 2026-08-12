package com.heallens.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.heallens.android.ui.theme.CyanPrimary
import com.heallens.android.ui.theme.DarkBackground
import com.heallens.android.ui.theme.SurfaceGlassBorder
import com.heallens.android.ui.theme.TextMuted
import com.heallens.android.ui.theme.TextPrimary
import com.heallens.android.ui.theme.TextSecondary
import com.heallens.android.ui.theme.glassmorphicCard
import com.heallens.android.utils.AppStrings
import com.heallens.android.utils.LanguageManager

@Composable
fun MedicalDisclaimerDialog(
    onAgree: () -> Unit
) {
    val activeLanguage by LanguageManager.currentLanguageFlow.collectAsState()
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = { /* Non-dismissable */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        // Fix 1: Full-Screen Dark Scrim (85% opacity dark scrim covering underlying UI completely)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            // Fix 2: Solid Opaque HealLens Dark Surface (Color(0xFF0F172A))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .glassmorphicCard(
                        cornerRadius = 24.dp,
                        backgroundColor = Color(0xFF0F172A)
                    )
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                ) {
                    // Header Row (Warning Icon + Title)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF59E0B).copy(alpha = 0.18f))
                                .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Medical AI Disclaimer",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = AppStrings.get("disclaimer_title", activeLanguage),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 18.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = SurfaceGlassBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Disclaimer Body
                    Text(
                        text = AppStrings.get("disclaimer_body", activeLanguage),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Emergency Warning Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.12f))
                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = AppStrings.get("disclaimer_emergency", activeLanguage),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFF87171),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 17.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action Button: "I Understand"
                    GradientButton(
                        text = AppStrings.get("disclaimer_btn_agree", activeLanguage),
                        onClick = onAgree,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
