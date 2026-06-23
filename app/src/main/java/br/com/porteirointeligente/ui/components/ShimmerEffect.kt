package br.com.porteirointeligente.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
private fun shimmerBrush(): Brush {
    val isDark = isSystemInDarkTheme()
    val shimmerColors = if (isDark) {
        listOf(Color(0xFF2E2E2E), Color(0xFF3D3D3D), Color(0xFF2E2E2E))
    } else {
        listOf(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            MaterialTheme.colorScheme.surfaceVariant
        )
    }

    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 1000f, 0f),
        end = Offset(translateAnim, 0f)
    )
}

@Composable
private fun ShimmerSurface(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.small
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(shimmerBrush())
    )
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.small
) {
    ShimmerSurface(modifier = modifier, shape = shape)
}

@Composable
fun ShimmerCircle(size: Dp = 48.dp) {
    ShimmerBox(modifier = Modifier.size(size), shape = CircleShape)
}

@Composable
fun ShimmerTextLine(
    modifier: Modifier = Modifier,
    width: Dp? = null,
    height: Dp = 14.dp
) {
    val wMod = if (width != null) Modifier.width(width) else Modifier.fillMaxWidth()
    ShimmerBox(modifier = modifier.then(wMod).height(height))
}

@Composable
fun ShimmerHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            ShimmerTextLine(width = 120.dp, height = 12.dp)
            Spacer(Modifier.height(8.dp))
            ShimmerTextLine(width = 180.dp, height = 24.dp)
            Spacer(Modifier.height(4.dp))
            ShimmerTextLine(width = 100.dp, height = 12.dp)
        }
        Spacer(Modifier.width(16.dp))
        ShimmerCircle(size = 68.dp)
    }
}

@Composable
fun ShimmerStatsCard() {
    ShimmerBox(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = MaterialTheme.shapes.large
    )
}

@Composable
fun ShimmerVisitItem() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(6.dp)
                .height(72.dp)
                .clip(MaterialTheme.shapes.small)
                .background(shimmerBrush())
        )
        Spacer(Modifier.width(12.dp))
        ShimmerCircle(size = 44.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            ShimmerTextLine(width = 140.dp)
            Spacer(Modifier.height(6.dp))
            ShimmerTextLine(width = 100.dp, height = 12.dp)
        }
        Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
            ShimmerTextLine(width = 40.dp, height = 14.dp)
            Spacer(Modifier.height(4.dp))
            ShimmerTextLine(width = 50.dp, height = 11.dp)
        }
    }
}
