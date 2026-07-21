package dev.solsynth.solian.data.api

import dev.solsynth.solian.data.TokenStore
import okhttp3.OkHttpClient
import okhttp3.Protocol
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
            // Force HTTP/1.1 to avoid proxy connection resets on Wear OS
            .protocols(listOf(Protocol.HTTP_1_1))
            .followRedirects(true)
            .followSslRedirects(true)
            // Auto-retry on transient socket drops
            .retryOnConnectionFailure(true)
            .addInterceptor(logging)
            // Close connection after each request to prevent stale socket errors
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Connection", "close")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(TokenStore.serverUrl.ensureTrailingSlash())
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun String.ensureTrailingSlash() =
        if (endsWith("/")) this else "$this/"
}
