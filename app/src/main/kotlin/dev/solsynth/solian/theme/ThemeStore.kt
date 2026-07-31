package dev.solsynth.solian.theme

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object ThemeStore {
    private const val PREFS_NAME = "solian_theme"
    private const val KEY_DYNAMIC_COLOR_ENABLED = "dynamic_color_enabled"
    private const val KEY_DYNAMIC_COLOR_AVAILABLE = "dynamic_color_available"

    private lateinit var prefs: android.content.SharedPreferences

    fun init(context: Context) {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    var isDynamicColorEnabled: Boolean
        get() = prefs.getBoolean(KEY_DYNAMIC_COLOR_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_DYNAMIC_COLOR_ENABLED, value).apply()

    var isDynamicColorAvailable: Boolean
        get() = prefs.getBoolean(KEY_DYNAMIC_COLOR_AVAILABLE, false)
        set(value) = prefs.edit().putBoolean(KEY_DYNAMIC_COLOR_AVAILABLE, value).apply()

    fun clear() {
        prefs.edit().clear().apply()
    }
}
