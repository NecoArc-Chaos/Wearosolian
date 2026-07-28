package dev.solsynth.solian.data

import android.content.Context
import android.content.SharedPreferences

object TokenStore {
    private const val PREFS_NAME = "solian_auth"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_TOKEN_EXPIRES_AT = "token_expires_at"
    private const val KEY_SERVER = "server_url"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_REFRESH_TOKEN, value).apply()

    var tokenExpiresAt: Long
        get() = prefs.getLong(KEY_TOKEN_EXPIRES_AT, 0L)
        set(value) = prefs.edit().putLong(KEY_TOKEN_EXPIRES_AT, value).apply()

    var serverUrl: String
        get() = prefs.getString(KEY_SERVER, "https://nt.solian.app") ?: "https://nt.solian.app"
        set(value) = prefs.edit().putString(KEY_SERVER, value).apply()

    fun isTokenValid(): Boolean {
        if (token.isNullOrBlank()) return false
        val expires = tokenExpiresAt
        if (expires == 0L) return true
        return System.currentTimeMillis() / 1000 < expires
    }

    val isLoggedIn: Boolean get() = !token.isNullOrBlank() && !refreshToken.isNullOrBlank() && isTokenValid()

    fun clear() {
        prefs.edit().clear().apply()
    }
}
