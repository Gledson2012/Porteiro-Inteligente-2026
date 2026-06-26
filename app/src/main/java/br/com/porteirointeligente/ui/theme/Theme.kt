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
    primary = Indigo60,
    onPrimary = Color.White,
    primaryContainer = Indigo40,
    onPrimaryContainer = Indigo90,
    secondary = Gold60,
    onSecondary = Gold10,
    secondaryContainer = Gold40,
    onSecondaryContainer = Gold90,
    tertiary = Teal60,
    onTertiary = Teal10,
    tertiaryContainer = Teal40,
    onTertiaryContainer = Teal90,
    error = Rose,
    onError = Color.White,
    errorContainer = RoseDark,
    onErrorContainer = RoseLight,
    background = SurfaceDark,
    onBackground = TextOnDark,
    surface = SurfaceContainerDark,
    onSurface = TextOnDark,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = TextMuted,
    outlineVariant = Color(0xFF2A3450),
    inverseSurface = SurfaceLight,
    inverseOnSurface = TextOnLight,
    inversePrimary = Indigo70,
    surfaceTint = Indigo60
)

private val LightColorScheme = lightColorScheme(
    primary = Indigo60,
    onPrimary = Color.White,
    primaryContainer = Indigo95,
    onPrimaryContainer = Indigo30,
    secondary = Gold60,
    onSecondary = Color.White,
    secondaryContainer = Gold95,
    onSecondaryContainer = Gold30,
    tertiary = Teal60,
    onTertiary = Color.White,
    tertiaryContainer = Teal95,
    onTertiaryContainer = Teal30,
    error = Rose,
    onError = Color.White,
    errorContainer = RoseLight,
    onErrorContainer = Color(0xFF8B1A1A),
    background = SurfaceLight,
    onBackground = TextOnLight,
    surface = SurfaceContainer,
    onSurface = TextOnLight,
    surfaceVariant = Color(0xFFF1F4FA),
    onSurfaceVariant = TextSecondary,
    outline = Slate300,
    outlineVariant = Slate200,
    inverseSurface = SurfaceDark,
    inverseOnSurface = TextOnDark,
    inversePrimary = Indigo80,
    surfaceTint = Indigo60
)

@Composable
fun PorteiroInteligenteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
