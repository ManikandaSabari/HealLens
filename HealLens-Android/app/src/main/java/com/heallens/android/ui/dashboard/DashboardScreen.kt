package com.heallens.android.ui.dashboard

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.PhotoCamera
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heallens.android.ui.components.GradientButton
import com.heallens.android.ui.theme.CyanPrimary
import com.heallens.android.ui.theme.DarkBackground
import com.heallens.android.ui.theme.PurpleAccent
import com.heallens.android.ui.theme.TextMuted
import com.heallens.android.ui.theme.TextPrimary
import com.heallens.android.ui.theme.TextSecondary
import com.heallens.android.ui.theme.glassmorphicCard
import com.heallens.android.utils.AppStrings
import com.heallens.android.utils.LanguageManager

@Composable
fun DashboardScreen(
    onNavigateToScanner: () -> Unit,
    onNavigateToChatbot: () -> Unit,
    onNavigateToReportAnalyzer: () -> Unit,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val activeLanguage by LanguageManager.currentLanguageFlow.collectAsState()

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
            // SECTION 1 — Welcome Header Banner Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphicCard(cornerRadius = 24.dp)
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = AppStrings.get("dashboard_welcome", activeLanguage),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary,
                                fontSize = 14.sp
                            )
                        )
                        Text(
                            text = "HealLens AI",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                fontSize = 24.sp
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(Color(0xFF10B981).copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = AppStrings.get("engine_online", activeLanguage),
                                color = Color(0xFF10B981),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = AppStrings.get("dashboard_subtitle", activeLanguage),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                GradientButton(
                    text = AppStrings.get("dashboard_quick_scan", activeLanguage) + " →",
                    onClick = onNavigateToScanner
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 2 — "Your Health, Smarter with HealLens AI" Informational Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphicCard(cornerRadius = 20.dp)
                    .padding(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyanPrimary.copy(alpha = 0.15f))
                            .border(1.dp, CyanPrimary.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = "Health Info",
                            tint = CyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = AppStrings.get("dashboard_about_title", activeLanguage),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = AppStrings.get("dashboard_about_desc", activeLanguage),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 3 — HEALLENS CAPABILITIES
            Text(
                text = AppStrings.get("dashboard_capabilities_title", activeLanguage),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 16.sp
                ),
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CapabilityCard(
                    title = AppStrings.get("cap_visual_title", activeLanguage),
                    subtitle = AppStrings.get("cap_visual_desc", activeLanguage),
                    icon = Icons.Default.PhotoCamera,
                    accentColor = CyanPrimary,
                    modifier = Modifier.weight(1f)
                )
                CapabilityCard(
                    title = AppStrings.get("cap_report_title", activeLanguage),
                    subtitle = AppStrings.get("cap_report_desc", activeLanguage),
                    icon = Icons.Default.Analytics,
                    accentColor = PurpleAccent,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CapabilityCard(
                    title = AppStrings.get("cap_assistant_title", activeLanguage),
                    subtitle = AppStrings.get("cap_assistant_desc", activeLanguage),
                    icon = Icons.Default.Android,
                    accentColor = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
                CapabilityCard(
                    title = AppStrings.get("cap_history_title", activeLanguage),
                    subtitle = AppStrings.get("cap_history_desc", activeLanguage),
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    accentColor = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 4 — QUICK ACTIONS
            Text(
                text = AppStrings.get("dashboard_quick_actions_title", activeLanguage),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 16.sp
                ),
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            ActionCard(
                title = AppStrings.get("quick_action_scanner_title", activeLanguage),
                subtitle = AppStrings.get("quick_action_scanner_desc", activeLanguage),
                icon = Icons.Default.PhotoCamera,
                accentColor = CyanPrimary,
                onClick = onNavigateToScanner
            )

            Spacer(modifier = Modifier.height(10.dp))

            ActionCard(
                title = AppStrings.get("quick_action_report_title", activeLanguage),
                subtitle = AppStrings.get("quick_action_report_desc", activeLanguage),
                icon = Icons.Default.Analytics,
                accentColor = PurpleAccent,
                onClick = onNavigateToReportAnalyzer
            )

            Spacer(modifier = Modifier.height(10.dp))

            ActionCard(
                title = AppStrings.get("quick_action_assistant_title", activeLanguage),
                subtitle = AppStrings.get("quick_action_assistant_desc", activeLanguage),
                icon = Icons.Default.Android,
                accentColor = Color(0xFF10B981),
                onClick = onNavigateToChatbot
            )

            Spacer(modifier = Modifier.height(10.dp))

            ActionCard(
                title = AppStrings.get("quick_action_history_title", activeLanguage),
                subtitle = AppStrings.get("quick_action_history_desc", activeLanguage),
                icon = Icons.AutoMirrored.Filled.Assignment,
                accentColor = Color(0xFFF59E0B),
                onClick = onNavigateToHistory
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CapabilityCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .glassmorphicCard(cornerRadius = 16.dp)
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(accentColor.copy(alpha = 0.15f))
                .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = subtitle,
            fontSize = 11.sp,
            color = TextMuted,
            lineHeight = 15.sp
        )
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassmorphicCard(cornerRadius = 16.dp)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accentColor.copy(alpha = 0.15f))
                .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextMuted,
                lineHeight = 16.sp
            )
        }

        Text(
            text = "→",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
