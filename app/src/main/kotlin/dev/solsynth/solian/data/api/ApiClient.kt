package dev.solsynth.solian.data.api

import dev.solsynth.solian.data.NetworkConfig
import dev.solsynth.solian.data.TokenStore
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

object ApiClient {

    var api: SolianApi = createApi()
        private set

    private val refreshApi: SolianApi by lazy { createApi() }

    fun recreate() {
        api = createApi()
    }

    /**
     * Shared OkHttpClient for WebSocket and other non-API uses.
     *
     * Uses the same certificate pinner and timeouts as the API client,
     * but without the auth interceptor (authentication is handled per-connection).
     */
    val httpClient: OkHttpClient = OkHttpClient.Builder()
        .protocols(listOf(Protocol.HTTP_1_1))
        .certificatePinner(NetworkConfig.getCertificatePinner())
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private fun createApi(): SolianApi {
        return build().create(SolianApi::class.java)
    }

    private fun build(): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }

        val client = OkHttpClient.Builder()
            .protocols(listOf(Protocol.HTTP_1_1))
            .followRedirects(true)
            .followSslRedirects(true)
            .retryOnConnectionFailure(false)
            .certificatePinner(NetworkConfig.getCertificatePinner())
            .addInterceptor(logging)
            .addInterceptor(AuthInterceptor(TokenStore, refreshApi))
            .addInterceptor { chain ->
                val original = chain.request()
                val request = if (TokenStore.isLoggedIn) {
                    original.newBuilder()
                        .header("Authorization", "Bearer ${TokenStore.token}")
                        .build()
                } else original
                val method = request.method
                if (method !in listOf("GET", "HEAD", "OPTIONS")) {
                    chain.proceed(request)
                } else {
                    val newRequest = request.newBuilder()
                        .header("Connection", "close")
                        .build()
                    chain.proceed(newRequest)
                }
            }
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
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
            val requestBuilder = original.newBuilder()

            if (tokenStore.isLoggedIn) {
                requestBuilder.header("Authorization", "Bearer ${tokenStore.token}")
            }

            var response = chain.proceed(requestBuilder.build())

            if (response.code == 401 && original.header("X-Auth-Retry") != "true") {
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

        @Suppress("BlockingMethodInNonBlockingContext")
        private fun refreshToken(api: SolianApi, store: TokenStore): String? {
            val refreshToken = store.refreshToken ?: run {
                store.clear()
                return null
            }

            return try {
                // OkHttp Interceptors run on the IO thread pool, so runBlocking here
                // blocks an IO thread, not the main thread. The ReentrantLock above
                // ensures only one request performs the refresh at a time.
                val tokenResp = runBlocking {
                    withTimeout(10_000) {
                        api.refreshToken(
                            mapOf(
                                "grant_type" to "refresh_token",
                                "refresh_token" to refreshToken
                            )
                        )
                    }
                }
                store.token = tokenResp.token
                // Persist the *new* refresh token returned by the server.
                // The previous value has already been used and must not be written back.
                tokenResp.refreshToken?.let { store.refreshToken = it }
                tokenResp.expiresIn?.let {
                    store.tokenExpiresAt = System.currentTimeMillis() / 1000 + it
                }
                tokenResp.token
            } catch (e: Exception) {
                store.clear()
                null
            }
        }
    }

    private fun String.ensureTrailingSlash() =
        if (endsWith("/")) this else "$this/"
}
