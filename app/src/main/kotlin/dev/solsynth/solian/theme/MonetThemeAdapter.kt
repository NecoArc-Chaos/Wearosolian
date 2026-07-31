package dev.solsynth.solian.theme

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import kotlin.math.pow

object MonetThemeAdapter {
    private const val TAG = "MonetThemeAdapter"
    private const val MIN_CONTRAST_RATIO = 4.5f

    fun adaptDynamicScheme(dynamicScheme: ColorScheme, isDark: Boolean): ColorScheme {
        return ColorScheme(
            primary = dynamicScheme.primary,
            onPrimary = ensureContrast(dynamicScheme.primary, OledBlack, MIN_CONTRAST_RATIO),
            primaryDim = dynamicScheme.primaryDim,
            primaryContainer = dynamicScheme.primaryContainer,
            onPrimaryContainer = ensureContrast(dynamicScheme.primaryContainer, OledBlack, MIN_CONTRAST_RATIO),
            secondary = dynamicScheme.secondary,
            secondaryDim = dynamicScheme.secondaryDim,
            onSecondary = ensureContrast(dynamicScheme.secondary, OledBlack, MIN_CONTRAST_RATIO),
            secondaryContainer = dynamicScheme.secondaryContainer,
            onSecondaryContainer = ensureContrast(dynamicScheme.secondaryContainer, OledBlack, MIN_CONTRAST_RATIO),
            tertiary = dynamicScheme.tertiary,
            tertiaryDim = dynamicScheme.tertiaryDim,
            onTertiary = ensureContrast(dynamicScheme.tertiary, OledBlack, MIN_CONTRAST_RATIO),
            onTertiaryContainer = ensureContrast(dynamicScheme.tertiaryContainer, OledBlack, MIN_CONTRAST_RATIO),
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

    internal fun ensureContrast(foreground: Color, background: Color, minRatio: Float): Color {
        val currentRatio = calculateContrastRatio(foreground, background)
        return if (currentRatio >= minRatio) {
            foreground
        } else {
            Log.w(TAG, "Contrast ratio $currentRatio is below minimum $minRatio, adjusting brightness")
            adjustBrightness(foreground, background, minRatio)
        }
    }

    internal fun calculateContrastRatio(color1: Color, color2: Color): Float {
        val lum1 = getRelativeLuminance(color1)
        val lum2 = getRelativeLuminance(color2)
        val lighter = maxOf(lum1, lum2)
        val darker = minOf(lum1, lum2)
        return (lighter + 0.05f) / (darker + 0.05f)
    }

    private fun getRelativeLuminance(color: Color): Float {
        val red = linearize(color.red)
        val green = linearize(color.green)
        val blue = linearize(color.blue)
        return 0.2126f * red + 0.7152f * green + 0.0722f * blue
    }

    private fun linearize(value: Float): Float {
        val corrected = if (value <= 0.04045f) {
            value / 12.92f
        } else {
            ((value + 0.055f) / 1.055f).pow(2.4f)
        }
        return corrected.coerceIn(0f, 1f)
    }

    private fun adjustBrightness(foreground: Color, background: Color, minRatio: Float): Color {
        var adjusted = foreground
        var ratio = calculateContrastRatio(adjusted, background)
        var factor = 1.2f

        while (ratio < minRatio && factor < 5f) {
            adjusted = adjustColorBrightness(foreground, factor)
            ratio = calculateContrastRatio(adjusted, background)
            factor += 0.3f
        }

        return adjusted
    }

    private fun adjustColorBrightness(color: Color, factor: Float): Color {
        return Color(
            red = (color.red * factor).coerceIn(0f, 1f),
            green = (color.green * factor).coerceIn(0f, 1f),
            blue = (color.blue * factor).coerceIn(0f, 1f),
            alpha = color.alpha,
        )
    }
}
