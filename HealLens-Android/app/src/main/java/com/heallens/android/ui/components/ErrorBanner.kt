package com.heallens.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heallens.android.ui.theme.SeverityCritical

@Composable
fun ErrorBanner(
    errorMessage: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x26EF4444))
            .border(1.dp, SeverityCritical, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Error",
            tint = SeverityCritical
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White,
                fontSize = 13.sp
            )
        )
    }
}
