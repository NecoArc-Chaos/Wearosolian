package dev.solsynth.solian.data.model

import com.google.gson.annotations.SerializedName

// ── Auth ──

object AuthConstants {
    const val PLATFORM_ANDROID = 3
    const val DEFAULT_SERVER_URL = "https://api.solian.app"
    const val NAMESPACE = "dev.solsynth.solian"
    const val DEFAULT_DEVICE_ID = "wearos-client"
    const val DEFAULT_DEVICE_NAME = "Wear OS"
    const val STATUS_TYPE_DEFAULT = "Default"
    const val STATUS_TYPE_INVISIBLE = "Invisible"
    const val STATUS_ATTITUDE_NEUTRAL = "Neutral"
    const val STATUS_ATTITUDE_BUSY = "Busy"
}

data class ChallengeRequest(
    val account: String,
    val platform: Int = AuthConstants.PLATFORM_ANDROID,
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("device_name") val deviceName: String? = AuthConstants.DEFAULT_DEVICE_NAME,
)

data class SnAuthChallenge(
    val id: String,
    @SerializedName("step_remain") val stepRemain: Int = 1,
    @SerializedName("approved_at") val approvedAt: String?,
    @SerializedName("expired_at") val expiredAt: String?,
    @SerializedName("step_total") val stepTotal: Int = 1,
    @SerializedName("failed_attempts") val failedAttempts: Int = 0,
    @SerializedName("blacklist_factors") val blacklistFactors: List<String> = emptyList(),
    val audiences: List<String> = emptyList(),
    val scopes: List<String> = emptyList(),
    @SerializedName("ip_address") val ipAddress: String?,
    @SerializedName("user_agent") val userAgent: String?,
    val nonce: String?,
    @SerializedName("country_code") val countryCode: String?,
    val country: String?,
    val city: String?,
    @SerializedName("device_id") val deviceId: String?,
    @SerializedName("device_name") val deviceName: String?,
    val platform: Int?,
    @SerializedName("declined_at") val declinedAt: String?,
    @SerializedName("approved_by_session_id") val approvedBySessionId: String?,
    @SerializedName("account_id") val accountId: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    @SerializedName("deleted_at") val deletedAt: String?,
)

data class SnAuthFactor(
    val id: String,
    val type: Int,
    val name: String?,
    @SerializedName("enabled_at") val enabledAt: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    @SerializedName("deleted_at") val deletedAt: String?,
    @SerializedName("expired_at") val expiredAt: String?,
    val trustworthy: Int = 0,
    @SerializedName("created_response") val createdResponse: Map<String, Any>? = null,
)

data class PerformChallengeRequest(
    @SerializedName("factor_id") val factorId: String,
    val password: String,
)

data class TokenExchangeRequest(
    @SerializedName("grant_type") val grantType: String = "authorization_code",
    val code: String,
)

data class TokenExchangeResponse(
    val token: String,
    @SerializedName("refresh_token") val refreshToken: String?,
    @SerializedName("expires_in") val expiresIn: Long?,
    @SerializedName("refresh_expires_in") val refreshExpiresIn: Long?,
)

// ── QR Login ──

data class QrGenerateRequest(
    @SerializedName("device_id") val deviceId: String = AuthConstants.DEFAULT_DEVICE_ID,
    @SerializedName("device_name") val deviceName: String? = AuthConstants.DEFAULT_DEVICE_NAME,
    val platform: Int = AuthConstants.PLATFORM_ANDROID,
    val audiences: List<String> = emptyList(),
    val scopes: List<String> = emptyList(),
)

data class QrGenerateResponse(
    @SerializedName("qr_challenge_id") val qrChallengeId: String,
    @SerializedName("auth_challenge_id") val authChallengeId: String,
    @SerializedName("qr_data") val qrData: String,
    @SerializedName("expires_at") val expiresAt: String?,
    @SerializedName("expires_in_seconds") val expiresInSeconds: Int?,
)

data class QrStatusResponse(
    @SerializedName("qr_challenge_id") val qrChallengeId: String,
    @SerializedName("auth_challenge_id") val authChallengeId: String,
    val status: String, // Pending, Scanned, Approved, Declined, Expired
    @SerializedName("expires_at") val expiresAt: String?,
    @SerializedName("approved_at") val approvedAt: String?,
)

// ── Account ──

data class SnAccount(
    val id: String,
    val name: String?,
    @SerializedName("nick") val nick: String?,
    @SerializedName("avatar_url") val avatarUrl: String?,
)

data class SnAccountStatus(
    val id: String,
    val type: String?,
    val attitude: String?,
    val label: String?,
    val symbol: String?,
    @SerializedName("is_online") val isOnline: Boolean?,
)

data class AccountStatusRequest(
    val type: String? = AuthConstants.STATUS_TYPE_DEFAULT,
    val attitude: String? = AuthConstants.STATUS_ATTITUDE_NEUTRAL,
    val label: String? = null,
    val symbol: String? = null,
)

// ── Post ──

data class SnPost(
    val id: String,
    val title: String?,
    val content: String?,
    val description: String?,
    val type: String?,
    val visibility: String?,
    @SerializedName("created_at") val createdAt: String?,
)

data class PostRequest(
    val content: String,
    val title: String? = null,
    val type: String? = null,
    val visibility: String? = null,
)

// ── Chat ──

data class SnChatRoom(
    val id: String,
    val name: String?,
    val description: String?,
    val type: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    val members: List<SnChatMember>?,
)

data class SnChatMember(
    val id: String,
    val name: String?,
    @SerializedName("nick") val nick: String?,
)

data class SnChatMessage(
    val id: String,
    val content: String?,
    val type: String?,
    @SerializedName("sender_id") val senderId: String?,
    @SerializedName("chat_room_id") val chatRoomId: String?,
    val sender: SnChatMember?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("room_sequence") val roomSequence: Long?,
    @SerializedName("reactions_count") val reactionsCount: Map<String, Int>?,
    @SerializedName("reactions_made") val reactionsMade: Map<String, String>?,
)

/** Backend /api/chat/summary returns Map<roomId, {unread_count, last_message, room}> */
data class ChatSummaryEntry(
    @SerializedName("unread_count") val unreadCount: Int = 0,
    @SerializedName("last_message") val lastMessage: SnChatMessage?,
    val room: SnChatRoom? = null,
)

data class SnChatSummary(
    val changes: List<ChatSummaryEntry>?,
    val summaries: Map<String, ChatSummaryEntry>?,
    @SerializedName("current_timestamp") val currentTimestamp: Long?,
    val total: Int?,
)

data class SnChatOnlineStatus(
    val id: String,
    val status: String?,
    @SerializedName("is_online") val isOnline: Boolean?,
)

data class SnChatGroup(
    val id: String,
    val name: String?,
    val color: String?,
    val icon: String?,
    val order: Int?,
)

data class SnRealtimeCall(
    val id: String,
    @SerializedName("room_id") val roomId: String?,
    @SerializedName("sender_id") val senderId: String?,
    @SerializedName("session_id") val sessionId: String?,
    @SerializedName("provider_name") val providerName: String?,
    @SerializedName("ended_at") val endedAt: String?,
    @SerializedName("created_at") val createdAt: String?,
)

// ── Chat Requests ──

data class CreateRoomRequest(
    val name: String?,
    val type: String?,
    @SerializedName("member_ids") val memberIds: List<String>? = null,
)

data class UpdateRoomRequest(
    val name: String? = null,
    val description: String? = null,
    val type: String? = null,
)

data class SendMessageRequest(
    val content: String,
    val attachments: List<Map<String, Any>>? = null,
)

data class EditMessageRequest(
    val content: String,
)

data class AddMemberRequest(
    @SerializedName("account_id") val accountId: String,
)

data class UpdateOnlineStatusRequest(
    val status: String,
)

data class CreateDirectChatRequest(
    @SerializedName("related_user_id") val relatedUserId: String,
)

data class CreateGroupRequest(
    val name: String,
    val color: String? = null,
    val icon: String? = null,
    val order: Int? = null,
)

data class UpdateGroupRequest(
    val name: String? = null,
    val color: String? = null,
    val icon: String? = null,
    val order: Int? = null,
)

data class InitiateCallRequest(
    val type: String,
)
