package br.com.porteirointeligente.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NeonBlue,
    onPrimary = Color.Black,
    primaryContainer = NeonBlueDark,
    onPrimaryContainer = NeonBlueLight,
    secondary = Navy400,
    onSecondary = Color.White,
    secondaryContainer = Navy700,
    onSecondaryContainer = Navy200,
    tertiary = Emerald,
    onTertiary = Color.Black,
    tertiaryContainer = EmeraldDark,
    onTertiaryContainer = Emerald,
    error = Rose,
    onError = Color.Black,
    errorContainer = RoseDark,
    onErrorContainer = Rose,
    background = BgDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceContainer,
    onSurfaceVariant = TextSecondary,
    outline = TextMuted,
    outlineVariant = SurfaceContainer,
    inverseSurface = SurfaceLight,
    inverseOnSurface = Color.Black,
    inversePrimary = NeonBlueDark,
    surfaceTint = NeonBlue
)

private val LightColorScheme = lightColorScheme(
    primary = NeonBlueDark,
    onPrimary = Color.White,
    primaryContainer = NeonBlueLight,
    onPrimaryContainer = Color(0xFF003640),
    secondary = Navy500,
    onSecondary = Color.White,
    secondaryContainer = Navy100,
    onSecondaryContainer = Navy900,
    tertiary = EmeraldDark,
    onTertiary = Color.White,
    error = RoseDark,
    onError = Color.White,
    background = SurfaceLight,
    onBackground = Color(0xFF1C1B1F),
    surface = SurfaceLight,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0)
)

@Composable
fun PorteiroInteligenteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Default false para usar nossa paleta customizada
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
