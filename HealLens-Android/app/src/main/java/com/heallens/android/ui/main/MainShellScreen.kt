package com.heallens.android.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heallens.android.ui.ayurveda.AyurvedaMapScreen
import com.heallens.android.ui.chatbot.HealthAssistantScreen
import com.heallens.android.ui.components.BottomNavigationBar
import com.heallens.android.ui.components.NavDestination
import com.heallens.android.ui.dashboard.DashboardScreen
import com.heallens.android.ui.history.ClinicalHistoryScreen
import com.heallens.android.ui.profile.ProfileScreen
import com.heallens.android.ui.profile.ProfileViewModel
import com.heallens.android.ui.report.ReportAnalyzerScreen
import com.heallens.android.ui.scanner.ScannerScreen
import com.heallens.android.ui.sos.EmergencySosScreen
import com.heallens.android.ui.theme.CyanPrimary
import com.heallens.android.ui.theme.DarkBackground
import com.heallens.android.ui.theme.PurpleAccent
import com.heallens.android.ui.theme.SurfaceGlass
import com.heallens.android.ui.theme.SurfaceGlassBorder
import com.heallens.android.ui.theme.TextMuted
import com.heallens.android.ui.theme.TextPrimary
import com.heallens.android.ui.theme.TextSecondary
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun MainShellScreen(
    viewModel: MainShellViewModel,
    profileViewModel: ProfileViewModel,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is MainShellEvent.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF0F172A),
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header Logo
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        Text(text = "🏥", fontSize = 26.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "HealLens AI",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CyanPrimary,
                                    fontSize = 20.sp
                                )
                            )
                            Text(
                                text = "Clinical Diagnostic System",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Language Selector Bar
                    val activeLanguage by com.heallens.android.utils.LanguageManager.currentLanguageFlow.collectAsState()
                    val context = androidx.compose.ui.platform.LocalContext.current

                    Text(
                        text = com.heallens.android.utils.AppStrings.get("profile_language_title", activeLanguage).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceGlass)
                            .border(1.dp, SurfaceGlassBorder, RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(
                            com.heallens.android.utils.AppLanguage.ENGLISH to "EN",
                            com.heallens.android.utils.AppLanguage.HINDI to "हिंदी",
                            com.heallens.android.utils.AppLanguage.TAMIL to "தமிழ்",
                            com.heallens.android.utils.AppLanguage.KANNADA to "ಕನ್ನಡ"
                        ).forEach { (langObj, labelText) ->
                            val isSelected = activeLanguage == langObj
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) CyanPrimary else Color.Transparent)
                                    .clickable {
                                        com.heallens.android.utils.LanguageManager.setLanguage(langObj, context)
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = labelText,
                                    color = if (isSelected) DarkBackground else TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "NAVIGATION CLINICAL MODULES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Navigation Drawer Items
                    NavDestination.values().forEach { destination ->
                        val isSelected = uiState.currentDestination == destination
                        val isEmergency = destination == NavDestination.EMERGENCY_SOS
                        val redColor = Color(0xFFEF4444)

                        val navLabelKey = when (destination) {
                            NavDestination.DASHBOARD -> "nav_dashboard"
                            NavDestination.SCANNER -> "nav_scanner"
                            NavDestination.CHATBOT -> "nav_chatbot"
                            NavDestination.REPORT_ANALYZER -> "nav_report"
                            NavDestination.HISTORY -> "nav_history"
                            NavDestination.AYURVEDA_MAP -> "nav_ayurveda"
                            NavDestination.EMERGENCY_SOS -> "nav_sos"
                            NavDestination.PROFILE -> "nav_profile"
                        }
                        val localizedLabel = com.heallens.android.utils.AppStrings.get(navLabelKey, activeLanguage)

                        NavigationDrawerItem(
                            label = {
                                Text(
                                    text = localizedLabel,
                                    fontWeight = if (isSelected || isEmergency) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = localizedLabel,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                viewModel.selectDestination(destination)
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = if (isEmergency) redColor.copy(alpha = 0.25f) else CyanPrimary.copy(alpha = 0.15f),
                                selectedIconColor = if (isEmergency) redColor else CyanPrimary,
                                selectedTextColor = if (isEmergency) redColor else CyanPrimary,
                                unselectedContainerColor = if (isEmergency) redColor.copy(alpha = 0.08f) else Color.Transparent,
                                unselectedIconColor = if (isEmergency) redColor else TextMuted,
                                unselectedTextColor = if (isEmergency) redColor else TextSecondary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.height(20.dp))

                    // Security Footer
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceGlass)
                            .border(1.dp, SurfaceGlassBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "🔒 HIPAA Compliant Diagnostic Data Encryption Active",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(SurfaceGlass)
                        .border(width = 1.dp, color = SurfaceGlassBorder)
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu Drawer",
                                tint = CyanPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = uiState.currentDestination.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                fontSize = 17.sp
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Online Status Badge
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF10B981).copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                .border(1.dp, Color(0xFF10B981).copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "AI Online",
                                    color = Color(0xFF10B981),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Logout Confirmation Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFEF4444).copy(alpha = 0.12f))
                                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .clickable { viewModel.showLogoutDialog(true) }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "Logout",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Logout",
                                    color = Color(0xFFEF4444),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Screen Content View
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (uiState.currentDestination) {
                        NavDestination.DASHBOARD -> DashboardScreen(
                            onNavigateToScanner = { viewModel.selectDestination(NavDestination.SCANNER) },
                            onNavigateToChatbot = { viewModel.selectDestination(NavDestination.CHATBOT) },
                            onNavigateToReportAnalyzer = { viewModel.selectDestination(NavDestination.REPORT_ANALYZER) },
                            onNavigateToHistory = { viewModel.selectDestination(NavDestination.HISTORY) }
                        )
                        NavDestination.SCANNER -> ScannerScreen()
                        NavDestination.CHATBOT -> HealthAssistantScreen()
                        NavDestination.REPORT_ANALYZER -> ReportAnalyzerScreen()
                        NavDestination.HISTORY -> ClinicalHistoryScreen()
                        NavDestination.AYURVEDA_MAP -> AyurvedaMapScreen()
                        NavDestination.EMERGENCY_SOS -> EmergencySosScreen()
                        NavDestination.PROFILE -> ProfileScreen(viewModel = profileViewModel)
                    }
                }

                // Mobile Bottom Navigation Bar
                BottomNavigationBar(
                    currentRoute = uiState.currentDestination.route,
                    onDestinationSelected = { destination ->
                        viewModel.selectDestination(destination)
                    }
                )
            }

            // Native Logout Alert Dialog
            if (uiState.showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.showLogoutDialog(false) },
                    title = {
                        Text(
                            text = "Confirm Sign Out",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    text = {
                        Text(
                            text = "Are you sure you want to log out of HealLens clinical session?",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { viewModel.performLogout() },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                        ) {
                            Text(text = "Sign Out", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { viewModel.showLogoutDialog(false) },
                            colors = ButtonDefaults.textButtonColors(contentColor = TextMuted)
                        ) {
                            Text(text = "Cancel")
                        }
                    },
                    containerColor = Color(0xFF10192D),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }
    }
}
