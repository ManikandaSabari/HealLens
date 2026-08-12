package com.heallens.android.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heallens.android.ui.theme.CyanPrimary
import com.heallens.android.ui.theme.SurfaceGlass
import com.heallens.android.ui.theme.SurfaceGlassBorder
import com.heallens.android.ui.theme.TextMuted
import com.heallens.android.utils.AppStrings
import com.heallens.android.utils.LanguageManager

enum class NavDestination(val route: String, val title: String, val icon: ImageVector) {
    DASHBOARD("dashboard", "Dashboard", Icons.Default.GridView),
    SCANNER("scanner", "Scanner", Icons.Default.PhotoCamera),
    CHATBOT("chatbot", "Assistant", Icons.Default.Android),
    REPORT_ANALYZER("report_analyzer", "Report", Icons.Default.Analytics),
    HISTORY("history", "History", Icons.Default.History),
    AYURVEDA_MAP("ayurveda_map", "Ayurveda Map", Icons.Default.Spa),
    EMERGENCY_SOS("emergency_sos", "SOS Emergency", Icons.Default.Warning),
    PROFILE("profile", "Profile", Icons.Default.Person)
}

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onDestinationSelected: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeLanguage by LanguageManager.currentLanguageFlow.collectAsState()

    val bottomBarDestinations = listOf(
        NavDestination.DASHBOARD,
        NavDestination.SCANNER,
        NavDestination.CHATBOT,
        NavDestination.REPORT_ANALYZER,
        NavDestination.HISTORY,
        NavDestination.PROFILE
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceGlass, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .border(
                width = 1.dp,
                color = SurfaceGlassBorder,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .padding(vertical = 8.dp, horizontal = 2.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        bottomBarDestinations.forEach { destination ->
            val isSelected = currentRoute == destination.route

            val iconColor by animateColorAsState(
                targetValue = if (isSelected) CyanPrimary else TextMuted,
                animationSpec = tween(durationMillis = 200),
                label = "NavIconColor"
            )

            val navKey = when (destination) {
                NavDestination.DASHBOARD -> "nav_dashboard"
                NavDestination.SCANNER -> "nav_scanner"
                NavDestination.CHATBOT -> "nav_chatbot"
                NavDestination.REPORT_ANALYZER -> "nav_report"
                NavDestination.HISTORY -> "nav_history"
                NavDestination.AYURVEDA_MAP -> "nav_ayurveda"
                NavDestination.EMERGENCY_SOS -> "nav_sos"
                NavDestination.PROFILE -> "nav_profile"
            }
            val titleText = AppStrings.get(navKey, activeLanguage)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onDestinationSelected(destination) }
                    .padding(vertical = 4.dp, horizontal = 2.dp)
            ) {
                Icon(
                    imageVector = destination.icon,
                    contentDescription = titleText,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = titleText,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = iconColor
                )

                if (isSelected) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Column(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(CyanPrimary)
                    ) {}
                }
            }
        }
    }
}
