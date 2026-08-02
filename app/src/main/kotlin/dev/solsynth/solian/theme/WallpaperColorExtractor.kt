package dev.solsynth.solian.theme

import android.content.Context
import androidx.wear.compose.material3.ColorScheme

interface WallpaperColorExtractor {
    fun isAvailable(): Boolean
    suspend fun extract(context: Context, isDark: Boolean): ColorScheme?
}
