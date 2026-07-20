package dev.solsynth.solian.data

import android.content.Context
import android.content.SharedPreferences

object TokenStore {
    private const val PREFS_NAME = "solian_auth"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_SERVER = "server_url"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    var serverUrl: String
        get() = prefs.getString(KEY_SERVER, "https://nt.solian.app") ?: "https://nt.solian.app"
        set(value) = prefs.edit().putString(KEY_SERVER, value).apply()

    val isLoggedIn: Boolean get() = !token.isNullOrBlank()
}
