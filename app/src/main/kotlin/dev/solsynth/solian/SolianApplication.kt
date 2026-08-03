package dev.solsynth.solian

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import dev.solsynth.solian.data.TokenStore
import dev.solsynth.solian.data.api.ApiClient

class SolianApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        TokenStore.init(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient {
                // Use the shared httpClient which has cert pinning
                // and add token if we have it.
                ApiClient.httpClient.newBuilder()
                    .addInterceptor { chain ->
                        val original = chain.request()
                        // Ensure we always have the latest token
                        val token = TokenStore.token
                        val request = if (!token.isNullOrBlank()) {
                            original.newBuilder()
                                .header("Authorization", "Bearer $token")
                                .build()
                        } else original
                        chain.proceed(request)
                    }
                    .build()
            }
            .crossfade(true)
            .build()
    }
}
