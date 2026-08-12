package com.heallens.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heallens.android.ui.theme.CyanPrimary
import com.heallens.android.ui.theme.TextPrimary
import com.heallens.android.ui.theme.TextSecondary

@Composable
fun DefaultCredentialsPill(
    onFillCredentials: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x0B00D4FF))
            .border(1.dp, Color(0x2600D4FF), RoundedCornerShape(14.dp))
            .clickable { onFillCredentials("admin@heallens.com", "admin123") }
            .padding(14.dp)
    ) {
        Text(
            text = "🔑",
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 1.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = "Authorized Portal Access Keys:",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            )

            Text(
                text = "Email: admin@heallens.com  |  Password: admin123",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = CyanPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.padding(top = 3.dp)
            )
        }
    }
}

