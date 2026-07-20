package dev.solsynth.solian.data.api

import dev.solsynth.solian.data.model.*
import retrofit2.http.*

interface SolianApi {

    // ── Auth (DysonNetwork /api/auth) ──
    @POST("api/auth/challenge")
    suspend fun createChallenge(@Body request: ChallengeRequest): SnAuthChallenge

    @GET("api/auth/challenge/{id}")
    suspend fun getChallenge(@Path("id") challengeId: String): SnAuthChallenge

    @POST("api/auth/token")
    suspend fun exchangeToken(@Body request: TokenExchangeRequest): TokenExchangeResponse

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body body: Map<String, String>): TokenExchangeResponse

    // ── Timeline (DysonNetwork /api/posts) ──
    @GET("api/posts")
    suspend fun getTimeline(
        @Header("Authorization") token: String,
        @Query("take") take: Int = 20,
    ): List<SnPost>
}
