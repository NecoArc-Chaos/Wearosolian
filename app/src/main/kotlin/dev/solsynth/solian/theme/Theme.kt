package dev.solsynth.solian.theme

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

val LocalScreenRound = staticCompositionLocalOf { false }

/** OLED-optimized color scheme, matching Wear Compose M3 1.6.2 API. */
val wearColorScheme = ColorScheme(
    primary = SolianViolet,
    onPrimary = OledBlack,
    primaryDim = SolianVioletDim,
    primaryContainer = SolianViolet,
    onPrimaryContainer = OledBlack,
    secondary = SolianViolet,
    secondaryDim = SolianVioletDim,
    onSecondary = OledBlack,
    secondaryContainer = SolianViolet,
    onSecondaryContainer = OledBlack,
    tertiary = SolianVioletBright,
    tertiaryDim = SolianVioletDim,
    onTertiary = OledBlack,
    tertiaryContainer = SolianViolet,
    onTertiaryContainer = OledBlack,
    background = OledBlack,
    onBackground = OnSurfaceHigh,
    onSurface = OnSurfaceHigh,
    onSurfaceVariant = OnSurfaceMedium,
    surfaceContainerLow = OledSurface,
    surfaceContainer = OledSurface,
    surfaceContainerHigh = OledSurfaceVariant,
    error = ErrorRed,
    onError = OledBlack,
)

@Composable
fun WearosolianTheme(content: @Composable () -> Unit) {
    val isRound = LocalConfiguration.current.isScreenRound

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as? Activity)?.window?.let { window ->
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    CompositionLocalProvider(LocalScreenRound provides isRound) {
        MaterialTheme(
            colorScheme = wearColorScheme,
            content = content
        )
    }
}
