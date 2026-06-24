package br.com.porteirointeligente.ui.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.porteirointeligente.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    var showSecondary by remember { mutableStateOf(false) }
    var showCredits by remember { mutableStateOf(false) }
    var showTitle by remember { mutableStateOf(false) }

    // Logo entrance animation
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "logoScale"
    )

    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "logoAlpha"
    )

    // Title entrance animation
    val titleAlpha by animateFloatAsState(
        targetValue = if (showTitle) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "titleAlpha"
    )

    val titleScale by animateFloatAsState(
        targetValue = if (showTitle) 1f else 0.9f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "titleScale"
    )

    // Subtitle animation
    val subtitleAlpha by animateFloatAsState(
        targetValue = if (showSecondary) 1f else 0f,
        animationSpec = tween(durationMillis = 700, delayMillis = 100),
        label = "subtitleAlpha"
    )

    // Credits animation
    val creditsAlpha by animateFloatAsState(
        targetValue = if (showCredits) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "creditsAlpha"
    )

    // Continuous pulse animation for the logo glow
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 1000f)
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(400)
        showTitle = true
        showSecondary = true
        delay(600)
        showCredits = true
        delay(1500)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Logo with continuous pulse
            Box(
                modifier = Modifier
                    .alpha(logoAlpha)
                    .scale(logoScale),
                contentAlignment = Alignment.Center
            ) {
                // Outer glow ring
                Surface(
                    modifier = Modifier.size(130.dp),
                    shape = RoundedCornerShape(32.dp),
                    color = Color.White.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // Pulse ring
                        Surface(
                            modifier = Modifier
                                .size(110.dp)
                                .scale(pulseScale),
                            shape = RoundedCornerShape(28.dp),
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                // Inner icon container
                                Surface(
                                    modifier = Modifier.size(96.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    color = Color.White.copy(alpha = 0.2f),
                                    shadowElevation = 8.dp
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                            contentDescription = "Porteiro Inteligente",
                                            modifier = Modifier
                                                .size(72.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))

            // Title with staggered scale + alpha animation
            Text(
                text = "Porteiro Inteligente",
                modifier = Modifier
                    .alpha(titleAlpha)
                    .scale(titleScale),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.5).sp
            )

            Spacer(Modifier.height(12.dp))

            // Subtitle
            Text(
                text = "Gestão de Portaria Simplificada",
                modifier = Modifier.alpha(subtitleAlpha),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            // Tagline
            Text(
                text = "QR Code • Visitas • Offline",
                modifier = Modifier.alpha(subtitleAlpha * 0.7f),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(80.dp))

            // Credits section
            Column(
                modifier = Modifier.alpha(creditsAlpha),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Criado por: Gledson Crist Ribeiro dos Santos",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "By Família Venâncio",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Light,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Version at bottom
        Text(
            text = "v 0.1.0",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.4f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(creditsAlpha)
        )
    }
}
