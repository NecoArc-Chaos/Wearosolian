package dev.solsynth.solian.theme

import android.content.Context
import androidx.wear.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], manifest = Config.NONE)
class ThemeStateManagerDynamicColorTest {

    private lateinit var fakeExtractor: FakeWallpaperColorExtractor
    private val testColorScheme = ColorScheme(
        primary = Color(0xFF6650A4),
        onPrimary = Color.Black,
        primaryDim = Color(0xFF6650A4),
        primaryContainer = Color(0xFF6650A4),
        onPrimaryContainer = Color.Black,
        secondary = Color(0xFF625B71),
        secondaryDim = Color(0xFF625B71),
        onSecondary = Color.Black,
        secondaryContainer = Color(0xFF625B71),
        onSecondaryContainer = Color.Black,
        tertiary = Color(0xFF7D5260),
        tertiaryDim = Color(0xFF7D5260),
        onTertiary = Color.Black,
        onTertiaryContainer = Color.Black,
        background = Color.Black,
        onBackground = Color.White,
        onSurface = Color.White,
        onSurfaceVariant = Color.Gray,
        surfaceContainerLow = Color.DarkGray,
        surfaceContainer = Color.DarkGray,
        surfaceContainerHigh = Color.DarkGray,
        error = Color.Red,
        onError = Color.Black,
    )

    @Before
    fun setUp() {
        val prefs = RuntimeEnvironment.getApplication()
            .getSharedPreferences("solian_theme_test_dyn", Context.MODE_PRIVATE)
        val field = dev.solsynth.solian.theme.ThemeStore::class.java.getDeclaredField("prefs")
        field.isAccessible = true
        field.set(null, prefs)

        fakeExtractor = FakeWallpaperColorExtractor(available = true, scheme = testColorScheme)
        ThemeStateManager.setExtractor(fakeExtractor)
        ThemeStateManager.cleanup()
    }

    @Test
    fun `updateDynamic with ColorScheme sets isDynamic true`() = runTest {
        ThemeStateManager.updateDynamic(testColorScheme)
        val state = ThemeStateManager.state.first()
        assertTrue(state.isDynamic)
        assertEquals(testColorScheme, state.colorScheme)
    }

    @Test
    fun `updateDynamic with null sets isDynamic false`() = runTest {
        ThemeStateManager.updateDynamic(testColorScheme)
        ThemeStateManager.updateDynamic(null)
        val state = ThemeStateManager.state.first()
        assertFalse(state.isDynamic)
        assertEquals(dev.solsynth.solian.theme.ActiveColorScheme, state.colorScheme)
    }

    @Test
    fun `FakeWallpaperColorExtractor returns configured scheme`() = runTest {
        val context = RuntimeEnvironment.getApplication()
        ThemeStore.isDynamicColorEnabled = true
        val result = fakeExtractor.extract(context, isDark = true)
        assertNotNull(result)
        assertEquals(testColorScheme, result)
    }

    @Test
    fun `FakeWallpaperColorExtractor returns null when unavailable`() = runTest {
        val unavailable = FakeWallpaperColorExtractor(available = false, scheme = null)
        val context = RuntimeEnvironment.getApplication()
        val result = unavailable.extract(context, isDark = true)
        assertTrue(result == null)
    }

    @Test
    fun `mapWallpaperColorValuesToColorScheme uses provided colors`() {
        val primary = Color(0xFF6650A4)
        val secondary = Color(0xFF625B71)
        val tertiary = Color(0xFF7D5260)
        val scheme = DynamicColorProvider.mapWallpaperColorValuesToColorScheme(
            primary = primary,
            secondary = secondary,
            tertiary = tertiary,
        )
        assertEquals(primary, scheme.primary)
        assertEquals(secondary, scheme.secondary)
        assertEquals(tertiary, scheme.tertiary)
    }
}
