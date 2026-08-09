package dev.solsynth.solian.data.api

import dev.solsynth.solian.data.TokenStore
import dev.solsynth.solian.data.model.AuthConstants
import dev.solsynth.solian.data.model.SnAttachment
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.concurrent.withLock
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

object ApiClient {

    private val refreshApi: SolianApi by lazy { createApi(withAuth = false) }

    var api: SolianApi = createApi(withAuth = true)
        private set

    @Suppress("unused")
    fun recreate() {
        api = createApi(withAuth = true)
    }

    /**
     * Shared OkHttpClient for WebSocket and other non-API uses.
     */
    val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private fun createApi(withAuth: Boolean): SolianApi {
        return build(withAuth).create(SolianApi::class.java)
    }

    private fun build(withAuth: Boolean): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .followRedirects(followRedirects = true)
            .followSslRedirects(true)
            .retryOnConnectionFailure(true)
            .addInterceptor(logging)
            .apply {
                if (withAuth) {
                    addInterceptor(AuthInterceptor(TokenStore, refreshApi))
                }
            }
            .addInterceptor { chain ->
                val original = chain.request()
                val path = original.url.encodedPath
                val builder = original.newBuilder()
                
                // Set global headers for v3 compatibility
                // Note: Only add MLS ability to non-auth requests to avoid 502 on Padlock
                if (!path.contains("padlock")) {
                    builder.header("X-Client-Ability", AuthConstants.MLS_CLIENT_ABILITY)
                }
                builder.header("X-Device-Id", TokenStore.deviceId)
                builder.header("Accept", "application/json")
                builder.header("User-Agent", "Solian/0.3.0 (Wear OS)")
                
                // Conditional Authorization: Don't send token to auth endpoints to prevent gateway confusion
                val token = TokenStore.token
                if (((!token.isNullOrBlank())) && (original.header("Authorization") == null)) {
                    if (!path.contains("padlock/auth")) {
                        builder.header("Authorization", "Bearer $token")
                    }
                }
                
                chain.proceed(builder.build())
            }
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(TokenStore.serverUrl.ensureTrailingSlash())
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private class AuthInterceptor(
        private val tokenStore: TokenStore,
        private val refreshApi: SolianApi,
    ) : Interceptor {

        private val lock = ReentrantLock()

        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()
            
            // Skip auth management for padlock (login/auth) to avoid loops
            if (original.url.encodedPath.contains("padlock/auth")) {
                return chain.proceed(original)
            }

            val requestBuilder = original.newBuilder()
            if (tokenStore.isLoggedIn) {
                requestBuilder.header("Authorization", "Bearer ${tokenStore.token}")
            }

            val response = chain.proceed(requestBuilder.build())

            if ((response.code == 401) && (original.header("X-Auth-Retry") != "true")) {
                val newToken = lock.withLock {
                    refreshToken(refreshApi, tokenStore)
                }

                if (newToken != null) {
                    response.close()
                    val newRequest = original.newBuilder()
                        .header("Authorization", "Bearer $newToken")
                        .header("X-Auth-Retry", "true")
                        .build()
                    return chain.proceed(newRequest)
                }
            }

            return response
        }

        private fun refreshToken(api: SolianApi, store: TokenStore): String? {
            val refreshToken = store.refreshToken ?: return null

            return try {
                val tokenResp = runBlocking {
                    withTimeout(10000.milliseconds) {
                        api.refreshToken(
                            mapOf(
                                "grant_type" to "refresh_token",
                                "refresh_token" to refreshToken,
                            ),
                        )
                    }
                }
                store.token = tokenResp.token
                tokenResp.refreshToken?.let { store.refreshToken = it }
                tokenResp.expiresIn?.let {
                    store.tokenExpiresAt = (System.currentTimeMillis() / 1000) + it
                }
                tokenResp.token
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun String.ensureTrailingSlash() =
        if (endsWith("/")) this else "$this/"

    fun resolveUrl(url: String?, attachment: SnAttachment? = null): String? {
        val targetUrl = url ?: attachment?.url ?: attachment?.previewUrl
        if (targetUrl.isNullOrBlank()) {
            attachment?.id?.let { id ->
                val rawBase = TokenStore.serverUrl.removeSuffix("/")
                return "$rawBase/drive/files/$id"
            }
            return null
        }
        
        if (targetUrl.startsWith("http")) return targetUrl
        val rawBase = TokenStore.serverUrl.removeSuffix("/")
        
        if (!targetUrl.contains("/") && !targetUrl.contains(".")) {
            return "$rawBase/drive/files/$targetUrl"
        }
        
        return if (targetUrl.startsWith("/")) "$rawBase$targetUrl" else "$rawBase/$targetUrl"
    }
}
