package dev.solsynth.solian.theme

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.dynamicColorScheme
import dev.solsynth.solian.theme.OledBlack
import dev.solsynth.solian.theme.OledSurface
import dev.solsynth.solian.theme.OledSurfaceVariant
import dev.solsynth.solian.theme.OnSurfaceHigh
import dev.solsynth.solian.theme.OnSurfaceMedium
import dev.solsynth.solian.theme.SolianViolet
import dev.solsynth.solian.theme.ErrorRed

val LocalScreenRound = staticCompositionLocalOf { false }

/**
 * OLED-optimized static color scheme for devices that don't support
 * dynamic colors, or when we want pure-black backgrounds.
 */
val wearColorScheme = ColorScheme(
    primary = SolianViolet,
    onPrimary = OledBlack,
    primaryContainer = SolianViolet,
    onPrimaryContainer = OledBlack,
    secondary = SolianViolet,
    onSecondary = OledBlack,
    background = OledBlack,
    onBackground = OnSurfaceHigh,
    surface = OledSurface,
    onSurface = OnSurfaceHigh,
    surfaceVariant = OledSurfaceVariant,
    onSurfaceVariant = OnSurfaceMedium,
    error = ErrorRed,
    onError = OledBlack,
    outline = OnSurfaceMedium,
)

@Composable
fun WearosolianTheme(
    content: @Composable () -> Unit
) {
    val isRound = LocalConfiguration.current.isScreenRound

    // Force OLED black window background for battery savings
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as? Activity)?.window?.let { window ->
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    val colorScheme = dynamicColorScheme(LocalContext.current)
        ?: wearColorScheme

    CompositionLocalProvider(LocalScreenRound provides isRound) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
