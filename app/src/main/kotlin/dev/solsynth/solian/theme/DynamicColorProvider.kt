package dev.solsynth.solian.theme

import android.app.WallpaperColors
import android.graphics.Color as AndroidColor
import androidx.wear.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

object DynamicColorProvider {

    @Suppress("DEPRECATION")
    internal fun mapWallpaperColorsToColorScheme(colors: WallpaperColors, isDark: Boolean): ColorScheme {
        val primary = colors.primaryColor?.toComposeColor() ?: SolianViolet
        val secondary = colors.secondaryColor?.toComposeColor() ?: SolianViolet
        val tertiary = colors.tertiaryColor?.toComposeColor() ?: SolianVioletBright

        return buildColorScheme(primary, secondary, tertiary)
    }

    internal fun mapWallpaperColorValuesToColorScheme(
        primary: Color,
        secondary: Color,
        tertiary: Color,
    ): ColorScheme {
        return buildColorScheme(primary, secondary, tertiary)
    }

    private fun buildColorScheme(primary: Color, secondary: Color, tertiary: Color): ColorScheme {
        return ColorScheme(
            primary = primary,
            onPrimary = OledBlack,
            primaryDim = primary.copy(alpha = 0.7f),
            primaryContainer = primary,
            onPrimaryContainer = OledBlack,
            secondary = secondary,
            secondaryDim = secondary.copy(alpha = 0.7f),
            onSecondary = OledBlack,
            secondaryContainer = secondary,
            onSecondaryContainer = OledBlack,
            tertiary = tertiary,
            tertiaryDim = tertiary.copy(alpha = 0.7f),
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
    }

    private fun AndroidColor.toComposeColor(): Color {
        return Color(this.toArgb())
    }
}

