package dev.solsynth.solian.theme

import android.content.ComponentCallbacks
import android.content.Context
import android.content.res.Configuration
import androidx.wear.compose.material3.ColorScheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ThemeState(
    val isDynamic: Boolean = false,
    val isDark: Boolean = true,
    val isAmbient: Boolean = false,
    val colorScheme: ColorScheme = ActiveColorScheme,
)

object ThemeStateManager {
    private const val TAG = "ThemeStateManager"
    private const val DEBOUNCE_MS = 500L

    private val _state = MutableStateFlow(ThemeState())
    val state: StateFlow<ThemeState> = _state

    private var componentCallbacks: ComponentCallbacks? = null
    private var lastUpdateTime = 0L
    private var extractor: WallpaperColorExtractor = SystemWallpaperColorExtractor()

    fun init(context: Context) {
        ThemeStore.init(context)

        componentCallbacks = object : ComponentCallbacks {
            @Suppress("OVERRIDE_DEPRECATION")
            override fun onConfigurationChanged(newConfig: Configuration) {
                handleConfigurationChanged(newConfig)
            }

            @Suppress("OVERRIDE_DEPRECATION")
            override fun onLowMemory() {}
        }

        context.registerComponentCallbacks(componentCallbacks!!)
        updateDarkMode()
    }

    fun updateAmbient(isAmbient: Boolean) {
        _state.value = _state.value.copy(isAmbient = isAmbient)
    }

    fun updateDynamic(dynamicColorScheme: ColorScheme?) {
        val isDynamic = dynamicColorScheme != null
        val colorScheme = dynamicColorScheme ?: ActiveColorScheme
        _state.value = _state.value.copy(isDynamic = isDynamic, colorScheme = colorScheme)
    }

    suspend fun refreshDynamicColor(context: Context) {
        updateDynamicColor(context)
    }

    fun cleanup() {
        componentCallbacks = null
    }

    internal fun handleConfigurationChanged(newConfig: Configuration) {
        val now = System.currentTimeMillis()
        if (now - lastUpdateTime < DEBOUNCE_MS) return
        lastUpdateTime = now

        updateDarkMode(newConfig)
    }

    private fun updateDarkMode(config: Configuration? = null) {
        val isDark = if (config != null) {
            (config.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        } else {
            true
        }
        _state.value = _state.value.copy(isDark = isDark)
    }

    internal suspend fun updateDynamicColor(context: Context) {
        val isAvailable = extractor.isAvailable()
        val isEnabled = ThemeStore.isDynamicColorEnabled && isAvailable

        ThemeStore.isDynamicColorAvailable = isAvailable

        if (isEnabled) {
            val isDark = _state.value.isDark
            val dynamicScheme = extractor.extract(context, isDark = isDark)
            val adapted = dynamicScheme?.let { MonetThemeAdapter.adaptDynamicScheme(it, isDark) }
            updateDynamic(adapted)
            ThemeStore.isDynamicColorEnabled = true
        } else {
            updateDynamic(null)
            ThemeStore.isDynamicColorEnabled = false
        }
    }

    fun setExtractor(newExtractor: WallpaperColorExtractor) {
        extractor = newExtractor
    }
}
