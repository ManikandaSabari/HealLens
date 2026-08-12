package com.heallens.android.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.glassmorphicCard(
    cornerRadius: Dp = 16.dp,
    backgroundColor: Color = SurfaceGlass,
    borderColor: Color = SurfaceGlassBorder
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(backgroundColor)
    .border(1.dp, borderColor, RoundedCornerShape(cornerRadius))

val PrimaryGradientBrush = Brush.horizontalGradient(
    colors = listOf(CyanPrimary, PurpleAccent)
)

val SecondaryGradientBrush = Brush.horizontalGradient(
    colors = listOf(BlueGradientStart, EmeraldGradientEnd)
)
