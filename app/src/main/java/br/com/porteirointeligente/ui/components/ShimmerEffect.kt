package br.com.porteirointeligente.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.porteirointeligente.ui.theme.Slate200
import br.com.porteirointeligente.ui.theme.Slate300
import br.com.porteirointeligente.ui.theme.Slate700

@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    width: Dp = 200.dp,
    height: Dp = 16.dp,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp)
) {
    val isDark = isSystemInDarkTheme()
    val shimmerColors = if (isDark) {
        listOf(Slate700.copy(alpha = 0.6f), Slate700.copy(alpha = 0.2f), Slate700.copy(alpha = 0.6f))
    } else {
        listOf(Slate200.copy(alpha = 0.6f), Color.White.copy(alpha = 0.6f), Slate200.copy(alpha = 0.6f))
    }

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 1200, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    Box(
        modifier = modifier.fillMaxWidth().height(height).clip(shape).background(brush)
    )
}

@Composable
fun ShimmerCard(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Slate700.copy(alpha = 0.3f) else Slate200.copy(alpha = 0.5f)

    Box(
        modifier = modifier.fillMaxWidth().height(120.dp).clip(shape).background(bgColor).padding(16.dp)
    ) {
        Column {
            ShimmerEffect(width = 120.dp, height = 14.dp, modifier = Modifier.fillMaxWidth(0.6f))
            ShimmerEffect(width = 80.dp, height = 12.dp, modifier = Modifier.fillMaxWidth(0.4f).padding(top = 12.dp))
            ShimmerEffect(width = 160.dp, height = 12.dp, modifier = Modifier.fillMaxWidth(0.8f).padding(top = 8.dp))
        }
    }
}

@Composable
fun ShimmerProfileCard() {
    val shape = RoundedCornerShape(16.dp)
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Slate700.copy(alpha = 0.3f) else Slate200.copy(alpha = 0.5f)

    Box(
        modifier = Modifier.fillMaxWidth().height(200.dp).clip(shape).background(bgColor).padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(80.dp).clip(CircleShape).background(if (isDark) Slate700.copy(alpha = 0.6f) else Slate200.copy(alpha = 0.8f))
        )
    }
}

@Composable
private fun isSystemInDarkTheme(): Boolean {
    val config = LocalConfiguration.current
    return config.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES
}
