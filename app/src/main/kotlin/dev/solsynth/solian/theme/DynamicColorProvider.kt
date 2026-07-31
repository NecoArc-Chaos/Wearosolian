package dev.solsynth.solian.theme

import android.content.Context
import android.app.WallpaperManager
import android.app.WallpaperColors
import android.graphics.Color as AndroidColor
import android.os.Build
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

object DynamicColorProvider {
    private const val TAG = "DynamicColorProvider"
    private const val TIMEOUT_MS = 500L

    fun isDynamicColorAvailable(context: Context): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }

    suspend fun getDynamicColorScheme(context: Context, isDark: Boolean): ColorScheme? {
        if (!isDynamicColorAvailable(context)) return null

        return try {
            withTimeout(TIMEOUT_MS) {
                withContext(Dispatchers.IO) {
                    val wallpaperManager = context.getSystemService(Context.WALLPAPER_SERVICE) as? WallpaperManager
                    val wallpaperColors = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                        @Suppress("DEPRECATION")
                        wallpaperManager?.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
                    } else {
                        null
                    }

                    if (wallpaperColors != null) {
                        mapWallpaperColorsToColorScheme(wallpaperColors, isDark)
                    } else {
                        Log.i(TAG, "Wallpaper colors not available, returning null")
                        null
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get dynamic color scheme", e)
            null
        }
    }

    @Suppress("DEPRECATION")
    internal fun mapWallpaperColorsToColorScheme(colors: WallpaperColors, isDark: Boolean): ColorScheme {
        val primary = colors.primaryColor?.toComposeColor() ?: SolianViolet
        val secondary = colors.secondaryColor?.toComposeColor() ?: SolianViolet
        val tertiary = colors.tertiaryColor?.toComposeColor() ?: SolianVioletBright

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
