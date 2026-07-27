package dev.solsynth.solian.data

import android.content.Context
import android.contentSharedPreferences

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
        if (expires == 0L) return true // no expiration
        return System.currentTimeMillis() / 1000 < expires
    }

    val isLoggedIn: Boolean get() = token.isNotNullOrBlank() && isTokenValid() && refreshToken.isNotNullOrBlank()

    fun clear() {
        prefs.edit().clear().apply()
    }

    // Helper: refresh token using refresh grant if available
    fun refreshTokenOrNull(): String? {
        if (!refreshToken.isNotNullOrBlank()) return null
        try {
            // Call refresh endpoint
            val resp = dev.solsynth.solian.data.api.ApiClient.api.refreshToken(
                mapOf(
                    "grant_type" to "refresh_token",
                    "refresh_token" to refreshToken!!
                )
            )
            if (!resp.token.isNullOrBlank()) {
                token = resp.token
                refreshToken = resp.refreshToken
                tokenExpiresAt = resp.expiresIn?.let { System.currentTimeMillis() / 1000 + it } ?: 0L
                return resp.token
            }
        } catch (e: Exception) {
            clear()
        }
        return null
    }
}

private fun String.isNotNullOrBlank() = !this.isNullOrBlank()
