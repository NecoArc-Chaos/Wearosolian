package dev.solsynth.solian.theme

import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DynamicColorMappingTest {

    private val testPrimary = Color(0xFF6650A4)
    private val testSecondary = Color(0xFF625B71)
    private val testTertiary = Color(0xFF7D5260)

    @Test
    fun `mapWallpaperColorValuesToColorScheme uses provided primary color`() {
        val scheme = DynamicColorProvider.mapWallpaperColorValuesToColorScheme(
            primary = testPrimary,
            secondary = testSecondary,
            tertiary = testTertiary,
        )

        assertEquals(testPrimary, scheme.primary)
        assertEquals(testPrimary, scheme.primaryContainer)
        assertEquals(testPrimary.copy(alpha = 0.7f), scheme.primaryDim)
    }

    @Test
    fun `mapWallpaperColorValuesToColorScheme uses provided secondary color`() {
        val scheme = DynamicColorProvider.mapWallpaperColorValuesToColorScheme(
            primary = testPrimary,
            secondary = testSecondary,
            tertiary = testTertiary,
        )

        assertEquals(testSecondary, scheme.secondary)
        assertEquals(testSecondary, scheme.secondaryContainer)
        assertEquals(testSecondary.copy(alpha = 0.7f), scheme.secondaryDim)
    }

    @Test
    fun `mapWallpaperColorValuesToColorScheme uses provided tertiary color`() {
        val scheme = DynamicColorProvider.mapWallpaperColorValuesToColorScheme(
            primary = testPrimary,
            secondary = testSecondary,
            tertiary = testTertiary,
        )

        assertEquals(testTertiary, scheme.tertiary)
        assertEquals(testTertiary.copy(alpha = 0.7f), scheme.tertiaryDim)
    }

    @Test
    fun `mapWallpaperColorValuesToColorScheme enforces OLED black background`() {
        val scheme = DynamicColorProvider.mapWallpaperColorValuesToColorScheme(
            primary = testPrimary,
            secondary = testSecondary,
            tertiary = testTertiary,
        )

        assertEquals(OledBlack, scheme.background)
        assertEquals(OledBlack, scheme.onPrimary)
        assertEquals(OledBlack, scheme.onPrimaryContainer)
        assertEquals(OledBlack, scheme.onSecondary)
        assertEquals(OledBlack, scheme.onSecondaryContainer)
        assertEquals(OledBlack, scheme.onTertiary)
        assertEquals(OledBlack, scheme.onTertiaryContainer)
        assertEquals(OledBlack, scheme.onError)
    }

    @Test
    fun `mapWallpaperColorValuesToColorScheme preserves onSurface contrast colors`() {
        val scheme = DynamicColorProvider.mapWallpaperColorValuesToColorScheme(
            primary = testPrimary,
            secondary = testSecondary,
            tertiary = testTertiary,
        )

        assertEquals(OnSurfaceHigh, scheme.onBackground)
        assertEquals(OnSurfaceHigh, scheme.onSurface)
        assertEquals(OnSurfaceMedium, scheme.onSurfaceVariant)
    }

    @Test
    fun `mapWallpaperColorValuesToColorScheme preserves OLED surface containers`() {
        val scheme = DynamicColorProvider.mapWallpaperColorValuesToColorScheme(
            primary = testPrimary,
            secondary = testSecondary,
            tertiary = testTertiary,
        )

        assertEquals(OledSurface, scheme.surfaceContainerLow)
        assertEquals(OledSurface, scheme.surfaceContainer)
        assertEquals(OledSurfaceVariant, scheme.surfaceContainerHigh)
    }

    @Test
    fun `mapWallpaperColorValuesToColorScheme preserves error color`() {
        val scheme = DynamicColorProvider.mapWallpaperColorValuesToColorScheme(
            primary = testPrimary,
            secondary = testSecondary,
            tertiary = testTertiary,
        )

        assertEquals(ErrorRed, scheme.error)
    }

    @Test
    fun `mapWallpaperColorValuesToColorScheme handles pure white colors`() {
        val white = Color.White
        val scheme = DynamicColorProvider.mapWallpaperColorValuesToColorScheme(
            primary = white,
            secondary = white,
            tertiary = white,
        )

        assertEquals(white, scheme.primary)
        assertEquals(OledBlack, scheme.onPrimary)
    }

    @Test
    fun `mapWallpaperColorValuesToColorScheme handles pure black colors`() {
        val black = Color.Black
        val scheme = DynamicColorProvider.mapWallpaperColorValuesToColorScheme(
            primary = black,
            secondary = black,
            tertiary = black,
        )

        assertEquals(black, scheme.primary)
        assertEquals(OledBlack, scheme.onPrimary)
    }

    @Test
    fun `mapWallpaperColorValuesToColorScheme returns non-null scheme for any input`() {
        val scheme = DynamicColorProvider.mapWallpaperColorValuesToColorScheme(
            primary = Color.Red,
            secondary = Color.Green,
            tertiary = Color.Blue,
        )

        assertNotNull(scheme)
    }
}
