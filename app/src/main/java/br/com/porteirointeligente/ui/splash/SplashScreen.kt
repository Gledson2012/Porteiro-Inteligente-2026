package br.com.porteirointeligente.ui.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
        targetValue = if (startAnimation) 1f else 0.4f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "logoScale"
    )

    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "logoAlpha"
    )

    // Title entrance animation
    val titleAlpha by animateFloatAsState(
        targetValue = if (showTitle) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "titleAlpha"
    )

    val titleScale by animateFloatAsState(
        targetValue = if (showTitle) 1f else 0.9f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "titleScale"
    )

    // Subtitle animation
    val subtitleAlpha by animateFloatAsState(
        targetValue = if (showSecondary) 1f else 0f,
        animationSpec = tween(durationMillis = 900, delayMillis = 100),
        label = "subtitleAlpha"
    )

    // Credits animation
    val creditsAlpha by animateFloatAsState(
        targetValue = if (showCredits) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "creditsAlpha"
    )

    // Continuous pulse animation for the logo glow
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Soft moving light glow animation
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val progressAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
        label = "progressAnim"
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
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Dynamic Ambient Glow Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Deep base background
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF0A0B10), Color(0xFF121422)),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                )
            )
            // Soft top-right gold glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFD700).copy(alpha = glowAlpha), Color.Transparent),
                    center = Offset(size.width * 0.8f, size.height * 0.2f),
                    radius = size.width * 0.7f
                )
            )
            // Soft bottom-left indigo/purple glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF4F46E5).copy(alpha = glowAlpha * 0.7f), Color.Transparent),
                    center = Offset(size.width * 0.2f, size.height * 0.8f),
                    radius = size.width * 0.7f
                )
            )
        }

        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Logo with glassmorphism + gold border
            Box(
                modifier = Modifier
                    .alpha(logoAlpha)
                    .scale(logoScale * pulseScale),
                contentAlignment = Alignment.Center
            ) {
                // Outer translucent ring
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .clip(RoundedCornerShape(36.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.25f),
                                    Color.Transparent,
                                    Color(0xFFFFD700).copy(alpha = 0.35f)
                                )
                            ),
                            shape = RoundedCornerShape(36.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Inner background container
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "Porteiro Inteligente",
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(48.dp))

            // Title with staggered scale + alpha animation
            Text(
                text = "Porteiro Inteligente",
                modifier = Modifier
                    .alpha(titleAlpha)
                    .scale(titleScale),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
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
                text = "QR CODE • VISITAS • OFFLINE",
                modifier = Modifier.alpha(subtitleAlpha * 0.7f),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                letterSpacing = 2.5.sp
            )

            Spacer(Modifier.height(56.dp))

            // Sleek loading progress bar
            Box(
                modifier = Modifier
                    .padding(horizontal = 48.dp)
                    .height(3.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progressAnim)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFFFBF00), Color(0xFFFFD700))
                            )
                        )
                )
            }

            Spacer(Modifier.height(56.dp))

            // Credits section
            Column(
                modifier = Modifier.alpha(creditsAlpha),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Criado por: Gledson Crist Ribeiro dos Santos",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "By Família Venâncio",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Light,
                    color = Color.White.copy(alpha = 0.45f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Version at bottom
        Text(
            text = "v 1.2.0",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.35f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(creditsAlpha)
        )
    }
}
