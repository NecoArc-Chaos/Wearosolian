package dev.solsynth.solian.theme

import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], manifest = Config.NONE)
class DynamicColorProviderTest {

    @Test
    fun `mapWallpaperColorValuesToColorScheme produces valid ColorScheme`() {
        val scheme = DynamicColorProvider.mapWallpaperColorValuesToColorScheme(
            primary = Color(0xFF6650A4),
            secondary = Color(0xFF625B71),
            tertiary = Color(0xFF7D5260),
        )

        assertEquals(Color(0xFF6650A4), scheme.primary)
        assertEquals(OledBlack, scheme.background)
        assertEquals(OledBlack, scheme.onPrimary)
        assertNotNull(scheme.error)
    }

    @Test
    fun `mapWallpaperColorValuesToColorScheme primary dim has 0_7 alpha`() {
        val scheme = DynamicColorProvider.mapWallpaperColorValuesToColorScheme(
            primary = Color(0xFF6650A4),
            secondary = Color(0xFF625B71),
            tertiary = Color(0xFF7D5260),
        )

        assertEquals(0.7f, scheme.primaryDim.alpha, 0.01f)
        assertEquals(0.7f, scheme.secondaryDim.alpha, 0.01f)
        assertEquals(0.7f, scheme.tertiaryDim.alpha, 0.01f)
    }

    @Test
    fun `mapWallpaperColorValuesToColorScheme preserves OLED surface containers`() {
        val scheme = DynamicColorProvider.mapWallpaperColorValuesToColorScheme(
            primary = Color(0xFF6650A4),
            secondary = Color(0xFF625B71),
            tertiary = Color(0xFF7D5260),
        )

        assertEquals(OledSurface, scheme.surfaceContainerLow)
        assertEquals(OledSurface, scheme.surfaceContainer)
        assertEquals(OledSurfaceVariant, scheme.surfaceContainerHigh)
    }

    @Test
    fun `mapWallpaperColorValuesToColorScheme handles monochrome input`() {
        val scheme = DynamicColorProvider.mapWallpaperColorValuesToColorScheme(
            primary = Color.White,
            secondary = Color.White,
            tertiary = Color.White,
        )

        assertEquals(Color.White, scheme.primary)
        assertEquals(OledBlack, scheme.onPrimary)
    }
}
