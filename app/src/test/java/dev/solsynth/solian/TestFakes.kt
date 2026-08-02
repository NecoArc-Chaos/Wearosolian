package dev.solsynth.solian.theme

import androidx.wear.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

class FakeWallpaperColorExtractor(
    private var available: Boolean = true,
    private var scheme: ColorScheme? = null,
) : WallpaperColorExtractor {

    override fun isAvailable(): Boolean = available

    override suspend fun extract(context: android.content.Context, isDark: Boolean): ColorScheme? =
        scheme

    fun setAvailable(value: Boolean) {
        available = value
    }

    fun setScheme(value: ColorScheme?) {
        scheme = value
    }
}
