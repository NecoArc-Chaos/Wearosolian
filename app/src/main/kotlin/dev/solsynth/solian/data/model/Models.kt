package dev.solsynth.solian.data.model

import com.google.gson.annotations.SerializedName

// ── Auth ──
data class ChallengeRequest(
    val account: String,
    val platform: String = "wearos",
    @SerializedName("device_id") val deviceId: String = "wearos",
    @SerializedName("device_name") val deviceName: String = "Wear OS",
)

data class SnAuthChallenge(
    val id: String,
    @SerializedName("expired_at") val expiredAt: String?,
    @SerializedName("step_total") val stepTotal: Int?,
    @SerializedName("account_id") val accountId: String?,
    val status: String? = null, // "pending", "approved", "declined"
)

data class TokenExchangeRequest(
    @SerializedName("grant_type") val grantType: String = "code",
    val code: String,
)

data class TokenExchangeResponse(
    val token: String,
    @SerializedName("refresh_token") val refreshToken: String?,
    @SerializedName("expires_in") val expiresIn: Long?,
)

// ── Post / Timeline ──
data class SnPost(
    val id: String,
    val body: String?,
    val author: SnAuthor?,
    @SerializedName("created_at") val createdAt: String?,
)

data class SnAuthor(
    val name: String,
    val avatar: String?,
)

// ── API Error ──
data class ApiError(
    val code: String?,
    val message: String?,
)
