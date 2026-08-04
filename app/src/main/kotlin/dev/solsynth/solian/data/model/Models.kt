package dev.solsynth.solian.data.model

import com.google.gson.annotations.SerializedName

// ── Auth ──

object AuthConstants {
    const val PLATFORM_ANDROID = 3
    const val DEFAULT_SERVER_URL = "https://api.solian.app"
    const val NAMESPACE = "dev.solsynth.solian"
    const val DEFAULT_DEVICE_ID = "wearos-client"
    const val DEFAULT_DEVICE_NAME = "Wear OS"
    const val STATUS_TYPE_NORMAL = 0
    const val STATUS_TYPE_BUSY = 1
    const val STATUS_TYPE_DND = 2
    const val STATUS_TYPE_INVISIBLE = 3
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
    @SerializedName("id_token") val idToken: String? = null,
    @SerializedName("token_type") val tokenType: String? = null,
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
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("nick") val nick: String? = null,
    @SerializedName("nickname") val nickname: String? = null,
    @SerializedName("username") val username: String? = null,
    @SerializedName("display_name") val displayName: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("avatar") val avatar: String? = null,
    @SerializedName("picture") val picture: SnAttachment? = null,
    @SerializedName("profile") val profile: SnProfile? = null,
    @SerializedName("status") val status: SnAccountStatus? = null,
)

data class SnProfile(
    val id: String,
    @SerializedName("first_name") val firstName: String?,
    @SerializedName("last_name") val lastName: String?,
    val bio: String?,
    @SerializedName("picture") val picture: SnAttachment? = null,
)

data class SnAccountStatus(
    val id: String,
    val type: Int = AuthConstants.STATUS_TYPE_NORMAL,
    val label: String? = null,
    val icon: String? = null,
    @SerializedName("clear_after") val clearAfter: String? = null,
    @SerializedName("is_online") val isOnline: Boolean? = null,
)

data class AccountStatusRequest(
    val type: Int? = AuthConstants.STATUS_TYPE_NORMAL,
    val label: String? = null,
    val icon: String? = null,
    @SerializedName("clear_after") val clearAfter: String? = null,
)

// ── Post ──

data class SnPost(
    val id: String,
    val title: String?,
    val content: String?,
    val description: String? = null,
    val type: Int? = 0,
    val visibility: Int? = 0,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("published_at") val publishedAt: String? = null,
    @SerializedName("author") val author: SnAccount? = null,
    @SerializedName("account") val account: SnAccount? = null,
    @SerializedName("user") val user: SnAccount? = null,
    @SerializedName("publisher") val publisher: SnAccount? = null,
    @SerializedName("creator") val creator: SnAccount? = null,
    @SerializedName("profile") val profile: SnAccount? = null,
    @SerializedName("attachments") val attachments: List<SnAttachment>? = null,
    @SerializedName("gallery") val gallery: List<SnAttachment>? = null,
    @SerializedName("media") val media: List<SnAttachment>? = null,
    @SerializedName("parent_id") val parentId: String? = null,
    @SerializedName("reply_to_id") val replyToId: String? = null,
    @SerializedName("replied_post_id") val repliedPostId: String? = null,
    @SerializedName("root_id") val rootId: String? = null,
    @SerializedName("parent") val parent: SnPost? = null,
    @SerializedName("reply_to") val replyTo: SnPost? = null,
    @SerializedName("replied_post") val repliedPost: SnPost? = null,
    @SerializedName("reply_count") val replyCount: Int? = 0,
    @SerializedName("replies_count") val repliesCount: Int? = 0,
    @SerializedName("thread_replies_count") val threadRepliesCount: Int? = 0,
)

data class SnAttachment(
    val id: String,
    val type: Int? = 0, // Image=0, Video=1, etc.
    @SerializedName("mime_type") val mimeType: String? = null,
    @SerializedName("url") val url: String?,
    @SerializedName("preview_url") val previewUrl: String?,
)

data class SnFortuneTip(
    @SerializedName("is_positive") val isPositive: Boolean,
    val title: String,
    val content: String,
)

data class SnCheckInFortuneReport(
    val version: Int = 1,
    val poem: String = "",
    val summary: String = "",
    @SerializedName("summary_detail") val summaryDetail: String? = null,
    val wish: String = "",
    val love: String = "",
    val study: String = "",
    val career: String = "",
    val health: String = "",
    @SerializedName("lost_item") val lostItem: String = "",
    @SerializedName("lucky_color") val luckyColor: String = "",
    @SerializedName("lucky_direction") val luckyDirection: String = "",
    @SerializedName("lucky_time") val luckyTime: String = "",
    @SerializedName("lucky_item") val luckyItem: String = "",
    @SerializedName("lucky_action") val luckyAction: String = "",
    @SerializedName("avoid_action") val avoidAction: String = "",
    val ritual: String = "",
)

data class SnCheckInResult(
    val id: String,
    val level: Int = 0,
    val tips: List<SnFortuneTip> = emptyList(),
    @SerializedName("fortune_report") val fortuneReport: SnCheckInFortuneReport? = null,
    @SerializedName("account_id") val accountId: String? = null,
    val account: SnAccount? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName("deleted_at") val deletedAt: String? = null,
)

data class PostRequest(
    val content: String,
    val title: String? = null,
    val type: String? = null,
    val visibility: String? = null,
)

// ── Chat ──

enum class ChatEncryptionMode(val value: Int) {
    None(0),
    E2eeMls(3);

    companion object {
        fun fromValue(value: Int): ChatEncryptionMode? = entries.find { it.value == value }
    }
}

data class SnChatRoom(
    val id: String,
    val name: String?,
    val description: String?,
    val type: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    val members: List<SnChatMember>?,
    @SerializedName("encryption_mode") val encryptionMode: Int? = null,
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
    @SerializedName("room_sequence") val roomSequence: Long?,
    @SerializedName("created_at") val createdAt: String? = null,
    val reactions_count: Map<String, Int>? = null,
    val reactions_made: Map<String, Boolean>? = null,
    @SerializedName("ciphertext") val ciphertext: String? = null,
    @SerializedName("encryption_epoch") val encryptionEpoch: Long? = null,
    @SerializedName("encryption_scheme") val encryptionScheme: String? = null,
)

/** Per-room summary inside the room list sync `summaries` map, keyed by room id. */
data class ChatSummaryEntry(
    @SerializedName("unread_count") val unreadCount: Int = 0,
    @SerializedName("last_message") val lastMessage: SnChatMessage? = null,
    val room: SnChatRoom? = null,
)

data class ChatSyncRequest(
    @SerializedName("last_sync_timestamp") val lastSyncTimestamp: Long = 0,
    @SerializedName("missing_sequences") val missingSequences: List<Long>? = null,
    @SerializedName("missing_sequence_ranges") val missingSequenceRanges: List<MissingSequenceRange>? = null,
)

data class MissingSequenceRange(
    @SerializedName("start_sequence") val startSequence: Long,
    @SerializedName("end_sequence") val endSequence: Long,
)

data class RoomSyncResponse(
    val changes: List<ChatSummaryEntry>? = null,
    val summaries: Map<String, ChatSummaryEntry>? = null,
    @SerializedName("current_timestamp") val currentTimestamp: Long? = null,
    @SerializedName("total_count") val totalCount: Int? = null,
)

data class RoomMessageSyncResponse(
    val messages: List<SnChatMessage>? = null,
    @SerializedName("current_timestamp") val currentTimestamp: Long? = null,
    @SerializedName("total_count") val totalCount: Int? = null,
)

data class SnChatSummary(
    @SerializedName("unread_count") val unreadCount: Int = 0,
    @SerializedName("last_message") val lastMessage: SnChatMessage? = null,
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

data class MarkAsReadRequest(
    @SerializedName("message_id") val messageId: String,
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
