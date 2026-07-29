package dev.solsynth.solian.theme

import android.app.Activity
import androidx.compose.runtime.*
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.contentColorFor

val LocalScreenRound = staticCompositionLocalOf { false }
val LocalIsAmbient = staticCompositionLocalOf { false }

@Composable
fun rememberIsScreenRound(): Boolean {
    val themeRound = LocalScreenRound.current
    val configRound = LocalConfiguration.current.isScreenRound
    return themeRound || configRound
}

@Composable
fun rememberIsAmbient(): Boolean {
    return LocalIsAmbient.current
}

/** OLED-optimized color scheme, matching Wear Compose M3 1.6.2 API. */
private val ActiveColorScheme = ColorScheme(
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

/** Ambient mode color scheme - desaturated, low-power. */
private val AmbientColorScheme = ColorScheme(
    primary = OnSurfaceMedium,
    onPrimary = OledBlack,
    primaryDim = OnSurfaceLow,
    primaryContainer = OledSurface,
    onPrimaryContainer = OnSurfaceMedium,
    secondary = OnSurfaceMedium,
    secondaryDim = OnSurfaceLow,
    onSecondary = OledBlack,
    secondaryContainer = OledSurface,
    onSecondaryContainer = OnSurfaceMedium,
    tertiary = OnSurfaceMedium,
    tertiaryDim = OnSurfaceLow,
    onTertiary = OledBlack,
    onTertiaryContainer = OnSurfaceMedium,
    background = OledBlack,
    onBackground = OnSurfaceMedium,
    onSurface = OnSurfaceMedium,
    onSurfaceVariant = OnSurfaceLow,
    surfaceContainerLow = OledSurface,
    surfaceContainer = OledSurface,
    surfaceContainerHigh = OledSurfaceVariant,
    error = ErrorRed,
    onError = OledBlack,
)

@Composable
fun WearosolianTheme(
    isAmbient: Boolean = false,
    content: @Composable () -> Unit,
) {
    val isRound = LocalConfiguration.current.isScreenRound
    val colorScheme = if (isAmbient) AmbientColorScheme else ActiveColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as? Activity)?.window?.let { window ->
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    CompositionLocalProvider(
        LocalScreenRound provides isRound,
        LocalIsAmbient provides isAmbient,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
