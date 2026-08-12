package com.heallens.android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heallens.android.ui.theme.SurfaceGlass
import com.heallens.android.ui.theme.SurfaceGlassBorder
import com.heallens.android.ui.theme.TextMuted
import com.heallens.android.ui.theme.TextPrimary

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(SurfaceGlassBorder)
        )
        Text(
            text = "  OR  ",
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextMuted,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(SurfaceGlassBorder)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceGlass)
            .border(1.dp, SurfaceGlassBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            GoogleIcon(modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Continue with Google",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            )
        }
    }
}

@Composable
private fun GoogleIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val bluePath = Path().apply {
            moveTo(width * 0.94f, height * 0.51f)
            cubicTo(width * 0.94f, height * 0.48f, width * 0.93f, height * 0.44f, width * 0.92f, height * 0.41f)
            lineTo(width * 0.5f, height * 0.41f)
            lineTo(width * 0.5f, height * 0.6f)
            lineTo(width * 0.75f, height * 0.6f)
            cubicTo(width * 0.74f, height * 0.66f, width * 0.7f, height * 0.72f, width * 0.65f, height * 0.75f)
            lineTo(width * 0.65f, height * 0.88f)
            lineTo(width * 0.8f, height * 0.88f)
            cubicTo(width * 0.89f, height * 0.79f, width * 0.94f, height * 0.66f, width * 0.94f, height * 0.51f)
            close()
        }
        drawPath(bluePath, Color(0xFF4285F4))

        val greenPath = Path().apply {
            moveTo(width * 0.5f, height * 0.96f)
            cubicTo(width * 0.62f, height * 0.96f, width * 0.73f, height * 0.92f, width * 0.8f, height * 0.85f)
            lineTo(width * 0.65f, height * 0.74f)
            cubicTo(width * 0.61f, height * 0.77f, width * 0.56f, height * 0.78f, width * 0.5f, height * 0.78f)
            cubicTo(width * 0.38f, height * 0.78f, width * 0.28f, height * 0.7f, width * 0.24f, height * 0.59f)
            lineTo(width * 0.09f, height * 0.59f)
            lineTo(width * 0.09f, height * 0.71f)
            cubicTo(width * 0.17f, height * 0.86f, width * 0.32f, height * 0.96f, width * 0.5f, height * 0.96f)
            close()
        }
        drawPath(greenPath, Color(0xFF34A853))

        val yellowPath = Path().apply {
            moveTo(width * 0.24f, height * 0.59f)
            cubicTo(width * 0.23f, height * 0.56f, width * 0.22f, height * 0.53f, width * 0.22f, height * 0.5f)
            cubicTo(width * 0.22f, height * 0.47f, width * 0.23f, height * 0.44f, width * 0.24f, height * 0.41f)
            lineTo(width * 0.24f, height * 0.29f)
            lineTo(width * 0.09f, height * 0.29f)
            cubicTo(width * 0.06f, height * 0.35f, width * 0.04f, height * 0.42f, width * 0.04f, height * 0.5f)
            cubicTo(width * 0.04f, height * 0.58f, width * 0.06f, height * 0.65f, width * 0.09f, height * 0.71f)
            lineTo(width * 0.24f, height * 0.59f)
            close()
        }
        drawPath(yellowPath, Color(0xFFFBBC05))

        val redPath = Path().apply {
            moveTo(width * 0.5f, height * 0.22f)
            cubicTo(width * 0.57f, height * 0.22f, width * 0.63f, height * 0.25f, width * 0.68f, height * 0.29f)
            lineTo(width * 0.81f, height * 0.16f)
            cubicTo(width * 0.73f, height * 0.09f, width * 0.62f, height * 0.04f, width * 0.5f, height * 0.04f)
            cubicTo(width * 0.32f, height * 0.04f, width * 0.17f, height * 0.14f, width * 0.09f, height * 0.29f)
            lineTo(width * 0.24f, height * 0.41f)
            cubicTo(width * 0.28f, height * 0.3f, width * 0.38f, height * 0.22f, width * 0.5f, height * 0.22f)
            close()
        }
        drawPath(redPath, Color(0xFFEA4335))
    }
}
