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

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body body: Map<String, String>): TokenExchangeResponse

    // ── Timeline ──

    @GET("api/posts")
    suspend fun getTimeline(
        @Query("take") take: Int = 20,
    ): List<SnPost>

    // ── Post Creation ──

    @POST("api/posts")
    suspend fun createPost(
        @Body request: PostRequest,
    ): SnPost

    // ── Chat ──

    @GET("api/chat")
    suspend fun getChatRooms(): ChatRoomsResponse

    @GET("api/chat/{roomId}/messages")
    suspend fun getMessages(
        @Path("roomId") roomId: String,
        @Query("take") take: Int = 20,
    ): List<SnChatMessage>

    // ── Account Status ──

    @GET("api/accounts/me/statuses")
    suspend fun getMyStatus(): SnAccountStatus

    @PATCH("api/accounts/me/statuses")
    suspend fun updateStatus(@Body request: AccountStatusRequest): SnAccountStatus
}

data class ChatRoomsResponse(
    val rooms: List<SnChatRoom>,
)
