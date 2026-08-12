package com.heallens.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.heallens.android.ui.theme.DarkBackground
import com.heallens.android.ui.theme.PrimaryGradientBrush

@Composable
fun BrandingLogo(
    size: Dp = 80.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(PrimaryGradientBrush),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size * 0.85f)
                .clip(CircleShape)
                .background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.HealthAndSafety,
                contentDescription = "HealLens Logo",
                tint = Color(0xFF00D4FF),
                modifier = Modifier.size(size * 0.5f)
            )
        }
    }
}
