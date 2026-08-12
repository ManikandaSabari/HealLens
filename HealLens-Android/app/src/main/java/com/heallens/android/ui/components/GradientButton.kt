package com.heallens.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.heallens.android.ui.theme.CyanPrimary
import com.heallens.android.ui.theme.PrimaryGradientBrush
import com.heallens.android.ui.theme.TextHeading

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    gradient: Brush = PrimaryGradientBrush
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (enabled) gradient else Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF1E293B))))
            .clickable(enabled = enabled && !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = TextHeading,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextHeading
                )
            )
        }
    }
}
