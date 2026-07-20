package dev.solsynth.solian.theme

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

val LocalScreenRound = staticCompositionLocalOf { false }

val wearColorScheme = ColorScheme(
    primary = SolianViolet,
    onPrimary = Color.Black,
    background = Color.Black,
    onBackground = OnSurfaceHigh,
    surface = Color.Black,
    onSurface = OnSurfaceHigh,
    error = ErrorRed,
    onError = Color.Black,
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
