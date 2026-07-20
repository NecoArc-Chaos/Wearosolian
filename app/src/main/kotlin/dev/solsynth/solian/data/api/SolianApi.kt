package dev.solsynth.solian.data.api

import dev.solsynth.solian.data.model.*
import retrofit2.http.*

interface SolianApi {

    // ── Auth ──

    @POST("api/auth/challenge")
    suspend fun createChallenge(@Body request: ChallengeRequest): SnAuthChallenge

    @GET("api/auth/challenge/{id}/factors")
    suspend fun getChallengeFactors(@Path("id") challengeId: String): List<SnAuthFactor>

    @POST("api/auth/challenge/{id}")
    suspend fun performChallenge(
        @Path("id") challengeId: String,
        @Body request: PerformChallengeRequest,
    ): SnAuthChallenge

    @POST("api/auth/token")
    suspend fun exchangeToken(@Body request: TokenExchangeRequest): TokenExchangeResponse

    // ── Timeline ──

    @GET("api/posts")
    suspend fun getTimeline(
        @Header("Authorization") token: String,
        @Query("take") take: Int = 20,
    ): List<SnPost>

    // ── Post Creation ──

    @POST("api/posts")
    suspend fun createPost(
        @Header("Authorization") token: String,
        @Body request: PostRequest,
    ): SnPost
}
