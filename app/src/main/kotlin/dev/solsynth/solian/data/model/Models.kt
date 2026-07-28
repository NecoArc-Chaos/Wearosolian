package dev.solsynth.solian.data.model

import com.google.gson.annotations.SerializedName

// ── Auth ──

data class ChallengeRequest(
    val account: String,
    val platform: Int = 3, // Android
    @SerializedName("device_id") val deviceId: String = "wearos-client",
    @SerializedName("device_name") val deviceName: String? = "Wear OS",
)

data class SnAuthChallenge(
    val id: String,
    @SerializedName("step_remain") val stepRemain: Int = 1,
    @SerializedName("approved_at") val approvedAt: String?,
)

data class SnAuthFactor(
    val id: String,
    val type: Int,
    val name: String?,
    @SerializedName("enabled_at") val enabledAt: String?,
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
    @SerializedName("device_id") val deviceId: String = "wearos-client",
    @SerializedName("device_name") val deviceName: String? = "Wear OS",
    val platform: Int = 3,
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
    val status: Int, // 0=Pending, 1=Scanned, 2=Approved, 3=Declined
    @SerializedName("expires_at") val expiresAt: String?,
    @SerializedName("approved_at") val approvedAt: String?,
)

// ── Post ──

data class SnPost(
    val id: String,
    val title: String?,
    val content: String?,
    val type: Int?,
    val visibility: Int?,
    val author: SnAuthor?,
    @SerializedName("created_at") val createdAt: String?,
)

data class SnAuthor(
    val id: String?,
    val name: String,
    val avatar: String?,
)

data class PostRequest(
    val content: String,
    val title: String? = null,
    val type: Int? = 0,
    val visibility: Int? = 0,
)

// ── Chat ──

data class SnChatRoom(
    val id: String,
    val name: String?,
    val lastMessage: String?,
    @SerializedName("updated_at") val updatedAt: String?,
)

data class SnChatMessage(
    val id: String,
    val content: String?,
    val sender: SnAuthor?,
    @SerializedName("created_at") val createdAt: String?,
)

data class ChatRoomsResponse(
    val rooms: List<SnChatRoom>,
)

// ── Account Status ──

data class SnAccountStatus(
    val id: String,
    val type: String?,
    val attitude: String?,
    val label: String?,
    val symbol: String?,
    @SerializedName("is_online") val isOnline: Boolean?,
)

data class AccountStatusRequest(
    val type: String? = "Default",
    val attitude: String? = "Neutral",
    val label: String? = null,
    val symbol: String? = null,
)
