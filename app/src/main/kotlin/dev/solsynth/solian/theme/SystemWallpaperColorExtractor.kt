package dev.solsynth.solian.theme

import android.app.WallpaperManager
import android.content.Context
import android.os.Build
import androidx.wear.compose.material3.ColorScheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class SystemWallpaperColorExtractor(
    private val timeoutMs: Long = 500L,
) : WallpaperColorExtractor {

    override fun isAvailable(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }

    override suspend fun extract(context: Context, isDark: Boolean): ColorScheme? {
        if (!isAvailable()) return null

        return try {
            withTimeout(timeoutMs) {
                withContext(Dispatchers.IO) {
                    val wallpaperManager =
                        context.getSystemService(Context.WALLPAPER_SERVICE) as? WallpaperManager
                    val wallpaperColors = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                        @Suppress("DEPRECATION")
                        wallpaperManager?.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
                    } else {
                        null
                    }

                    if (wallpaperColors != null) {
                        DynamicColorProvider.mapWallpaperColorsToColorScheme(
                            wallpaperColors,
                            isDark,
                        )
                    } else {
                        null
                    }
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}
