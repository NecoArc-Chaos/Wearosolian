package dev.solsynth.solian.data.api

import dev.solsynth.solian.data.model.*
import retrofit2.http.*

interface SolianApi {

    // ── Auth ──

    @POST("padlock/auth/challenge")
    suspend fun createChallenge(@Body request: ChallengeRequest): SnAuthChallenge

    @GET("padlock/auth/challenge/{id}/factors")
    suspend fun getChallengeFactors(@Path("id") challengeId: String): List<SnAuthFactor>

    @PATCH("padlock/auth/challenge/{id}")
    suspend fun performChallenge(
        @Path("id") challengeId: String,
        @Body request: PerformChallengeRequest,
    ): SnAuthChallenge

    @POST("padlock/auth/token")
    suspend fun exchangeToken(@Body request: TokenExchangeRequest): TokenExchangeResponse

    @POST("padlock/auth/refresh")
    suspend fun refreshToken(@Body body: Map<String, String>): TokenExchangeResponse

    // ── QR Login ──

    @POST("padlock/auth/qr/generate")
    suspend fun generateQrChallenge(@Body request: QrGenerateRequest): QrGenerateResponse

    @GET("padlock/auth/qr/{id}")
    suspend fun getQrStatus(@Path("id") qrChallengeId: String): QrStatusResponse

    // ── Account ──

    @GET("passport/accounts/me")
    suspend fun getMe(): SnAccount

    @GET("passport/accounts/me/statuses")
    suspend fun getMyStatus(): SnAccountStatus

    @PATCH("passport/accounts/me/statuses")
    suspend fun updateStatus(@Body request: AccountStatusRequest): SnAccountStatus

    // ── Check-in ──

    @GET("passport/accounts/me/check-in")
    suspend fun getCheckInResult(@Query("version") version: Int = 2): retrofit2.Response<SnCheckInResult>

    @POST("passport/accounts/me/check-in")
    suspend fun checkIn(@Query("version") version: Int = 2): retrofit2.Response<Unit>

    // ── Timeline ──

    @GET("sphere/timeline/home")
    suspend fun getTimeline(
        @Query("take") take: Int = 20,
        @Query("offset") offset: Int = 0,
    ): List<SnPost>

    // ── Post Creation ──

    @POST("sphere/posts")
    suspend fun createPost(@Body request: PostRequest): SnPost

    // ── Chat ──

    @GET("messager/chat/summary")
    suspend fun getChatSummary(): SnChatSummary

    @POST("messager/chat/rooms/sync")
    suspend fun syncRooms(@Body request: ChatSyncRequest): RoomSyncResponse

    @POST("messager/chat/{roomId}/sync")
    suspend fun syncRoom(
        @Path("roomId") roomId: String,
        @Body request: ChatSyncRequest,
    ): RoomMessageSyncResponse

    @POST("messager/chat/sync")
    suspend fun syncAll(@Body request: ChatSyncRequest): RoomMessageSyncResponse

    @GET("messager/chat/{roomId}/messages")
    suspend fun getMessages(
        @Path("roomId") roomId: String,
        @Query("offset") offset: Int = 0,
        @Query("take") take: Int = 20,
    ): List<SnChatMessage>

    @GET("messager/chat/rooms")
    suspend fun getRooms(
        @Query("offset") offset: Int = 0,
        @Query("take") take: Int = 20,
    ): List<SnChatRoom>

    @GET("messager/chat/rooms/{roomId}")
    suspend fun getRoom(@Path("roomId") roomId: String): SnChatRoom

    @POST("messager/chat/rooms")
    suspend fun createRoom(@Body request: CreateRoomRequest): SnChatRoom

    @PATCH("messager/chat/rooms/{roomId}")
    suspend fun updateRoom(
        @Path("roomId") roomId: String,
        @Body request: UpdateRoomRequest,
    ): SnChatRoom

    @DELETE("messager/chat/rooms/{roomId}")
    suspend fun deleteRoom(@Path("roomId") roomId: String)

    @POST("messager/chat/rooms/{roomId}/messages")
    suspend fun sendMessage(
        @Path("roomId") roomId: String,
        @Body request: SendMessageRequest,
    ): SnChatMessage

    @PATCH("messager/chat/rooms/{roomId}/messages/{messageId}")
    suspend fun editMessage(
        @Path("roomId") roomId: String,
        @Path("messageId") messageId: String,
        @Body request: EditMessageRequest,
    ): SnChatMessage

    @DELETE("messager/chat/rooms/{roomId}/messages/{messageId}")
    suspend fun deleteMessage(
        @Path("roomId") roomId: String,
        @Path("messageId") messageId: String,
    )

    @POST("messager/chat/rooms/{roomId}/read")
    suspend fun markAsRead(
        @Path("roomId") roomId: String,
        @Body request: MarkAsReadRequest,
    )

    @GET("messager/chat/rooms/{roomId}/members")
    suspend fun getMembers(
        @Path("roomId") roomId: String,
        @Query("offset") offset: Int = 0,
        @Query("take") take: Int = 50,
    ): List<SnChatMember>

    @POST("messager/chat/rooms/{roomId}/members")
    suspend fun addMember(
        @Path("roomId") roomId: String,
        @Body request: AddMemberRequest,
    )

    @DELETE("messager/chat/rooms/{roomId}/members/{accountId}")
    suspend fun removeMember(
        @Path("roomId") roomId: String,
        @Path("accountId") accountId: String,
    )

    @DELETE("messager/chat/rooms/{roomId}/members/me")
    suspend fun leaveRoom(@Path("roomId") roomId: String)

    @GET("messager/chat/online")
    suspend fun getOnlineStatus(
        @Query("account_ids") accountIds: String,
    ): List<SnChatOnlineStatus>

    @POST("messager/chat/online")
    suspend fun updateOnlineStatus(@Body request: UpdateOnlineStatusRequest)

    @GET("messager/chat/direct/{accountId}")
    suspend fun getDirectChat(@Path("accountId") accountId: String): SnChatRoom?

    @POST("messager/chat/direct")
    suspend fun createDirectChat(@Body request: CreateDirectChatRequest): SnChatRoom

    @GET("messager/chat/groups")
    suspend fun getGroups(): List<SnChatGroup>

    @POST("messager/chat/groups")
    suspend fun createGroup(@Body request: CreateGroupRequest): SnChatGroup

    @PATCH("messager/chat/groups/{groupId}")
    suspend fun updateGroup(
        @Path("groupId") groupId: String,
        @Body request: UpdateGroupRequest,
    ): SnChatGroup

    @DELETE("messager/chat/groups/{groupId}")
    suspend fun deleteGroup(@Path("groupId") groupId: String)

    @POST("messager/chat/rooms/{roomId}/calls")
    suspend fun initiateCall(
        @Path("roomId") roomId: String,
        @Body request: InitiateCallRequest,
    ): SnRealtimeCall

    @POST("messager/chat/rooms/{roomId}/calls/{callId}/join")
    suspend fun joinCall(
        @Path("roomId") roomId: String,
        @Path("callId") callId: String,
    )

    @POST("messager/chat/rooms/{roomId}/calls/{callId}/leave")
    suspend fun leaveCall(
        @Path("roomId") roomId: String,
        @Path("callId") callId: String,
    )

    @DELETE("messager/chat/rooms/{roomId}/calls/{callId}")
    suspend fun endCall(
        @Path("roomId") roomId: String,
        @Path("callId") callId: String,
    )
}
