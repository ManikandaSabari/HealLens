package com.heallens.android.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heallens.android.ui.components.BrandingLogo
import com.heallens.android.ui.theme.CyanPrimary
import com.heallens.android.ui.theme.DarkBackground
import com.heallens.android.ui.theme.PurpleAccent
import com.heallens.android.ui.theme.TextMuted
import com.heallens.android.ui.theme.TextSecondary
import com.heallens.android.utils.Constants
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SplashScreen(
    viewModel: SplashViewModel,
    onNavigateNext: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scale = remember { Animatable(0.8f) }

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is SplashEvent.NavigateTo -> onNavigateNext(event.route)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            BrandingLogo(
                size = 110.dp,
                modifier = Modifier.scale(scale.value)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = Constants.APP_NAME,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 36.sp,
                    brush = Brush.horizontalGradient(listOf(CyanPrimary, PurpleAccent))
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = Constants.APP_TAGLINE,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = CyanPrimary,
                strokeWidth = 3.dp,
                modifier = Modifier.scale(0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = uiState.statusText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextMuted,
                    fontSize = 12.sp
                )
            )
        }
    }
}
