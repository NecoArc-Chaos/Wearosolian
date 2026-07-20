package dev.solsynth.solian.data.api

import dev.solsynth.solian.data.model.*
import retrofit2.http.*

interface SolianApi {

    @POST("padlock/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("id/posts")
    suspend fun getTimeline(
        @Header("Authorization") token: String,
        @Query("take") take: Int = 20,
    ): List<SnPost>

    @GET("api/server/capabilities")
    suspend fun getServerCapabilities(): ServerCapabilities
}
