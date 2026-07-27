package dev.solsynth.solian.data.api

import dev.solsynth.solian.data.TokenStore
import okhttp3.Authenticator
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    var api: SolianApi = build().create(SolianApi::class.java)
        private set

    fun recreate() {
        api = build().create(SolianApi::class.java)
    }

    private fun build(): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }

        val client = OkHttpClient.Builder()
            .protocols(listOf(Protocol.HTTP_1_1))
            .followRedirects(true)
            .followSslRedirects(true)
            // Disable OkHttp's built-in retry to prevent duplicate POST/PATCH/DELETE
            .retryOnConnectionFailure(false)
            .authenticator(tokenAuthenticator)
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

    private val tokenAuthenticator = Authenticator { route: okhttp3.Route?, response: Response ->
        val refreshToken = TokenStore.refreshToken ?: return@Authenticator null
        if (response.request.header("Authorization")?.startsWith("Bearer ") != true) {
            return@Authenticator null
        }

        val newToken = try {
            val tokenResp = api.refreshToken(
                mapOf(
                    "grant_type" to "refresh_token",
                    "refresh_token" to refreshToken
                )
            )
            TokenStore.token = tokenResp.token
            tokenResp.refreshToken?.let { TokenStore.refreshToken = it }
            tokenResp.expiresIn?.let {
                TokenStore.tokenExpiresAt = System.currentTimeMillis() / 1000 + it
            }
            tokenResp.token
        } catch (e: Exception) {
            TokenStore.clear()
            null
        } ?: return@Authenticator null

        response.request.newBuilder()
            .header("Authorization", "Bearer $newToken")
            .build()
    }

    private fun String.ensureTrailingSlash() =
        if (endsWith("/")) this else "$this/"
}
