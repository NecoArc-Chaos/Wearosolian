package dev.solsynth.solian

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.memory.MemoryCache
import dev.solsynth.solian.data.CrashReport
import dev.solsynth.solian.data.TokenStore
import dev.solsynth.solian.data.api.ApiClient

class SolianApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        CrashReport.install(this)
        TokenStore.init(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient {
                ApiClient.httpClient.newBuilder()
                    .addInterceptor { chain ->
                        val original = chain.request()
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
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.1)
                    .build()
            }
            .allowHardware(false)
            .crossfade(true)
            .build()
    }
}
