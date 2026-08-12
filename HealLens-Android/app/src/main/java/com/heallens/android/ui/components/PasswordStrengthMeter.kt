package com.heallens.android.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heallens.android.ui.theme.TextMuted

enum class PasswordStrength(val label: String, val level: Int, val color: Color) {
    EMPTY("", 0, Color.Transparent),
    WEAK("Weak", 1, Color(0xFFEF4444)),
    MEDIUM("Medium", 2, Color(0xFFF59E0B)),
    STRONG("Strong", 3, Color(0xFF10B981))
}

fun calculatePasswordStrength(password: String): PasswordStrength {
    if (password.isEmpty()) return PasswordStrength.EMPTY
    if (password.length < 6) return PasswordStrength.WEAK

    var score = 0
    if (password.length >= 8) score++
    if (password.length >= 12) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isLowerCase() }) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++

    return when {
        score >= 4 -> PasswordStrength.STRONG
        score >= 2 -> PasswordStrength.MEDIUM
        else -> PasswordStrength.WEAK
    }
}

@Composable
fun PasswordStrengthMeter(
    password: String,
    modifier: Modifier = Modifier
) {
    val strength = calculatePasswordStrength(password)

    if (strength == PasswordStrength.EMPTY) return

    val animatedColor by animateColorAsState(
        targetValue = strength.color,
        animationSpec = tween(durationMillis = 300),
        label = "PasswordStrengthColor"
    )

    val progressFraction = when (strength) {
        PasswordStrength.EMPTY -> 0f
        PasswordStrength.WEAK -> 0.33f
        PasswordStrength.MEDIUM -> 0.66f
        PasswordStrength.STRONG -> 1f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 300),
        label = "PasswordStrengthProgress"
    )

    Column(modifier = modifier.fillMaxWidth().padding(top = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Password Strength",
                fontSize = 11.sp,
                color = TextMuted
            )
            Text(
                text = strength.label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = animatedColor
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF1E293B))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(RoundedCornerShape(2.dp))
                    .background(animatedColor)
            )
        }
    }
}
