package dev.solsynth.solian.data.model

import com.google.gson.annotations.SerializedName

// ── Auth ──

data class ChallengeRequest(
    val account: String,
    val platform: Int = 0, // 0 = WearOS in ClientPlatform enum
    @SerializedName("device_id") val deviceId: String = "wearos",
    @SerializedName("device_name") val deviceName: String = "Wear OS",
)

data class SnAuthChallenge(
    val id: String,
    @SerializedName("expired_at") val expiredAt: String?,
    @SerializedName("step_total") val stepTotal: Int?,
)

data class TokenExchangeRequest(
    @SerializedName("grant_type") val grantType: String = "authorization_code",
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
    val content: String?,
    val body: String?,
    val author: SnAuthor?,
    @SerializedName("created_at") val createdAt: String?,
)

data class SnAuthor(
    val name: String,
    val avatar: String?,
)

// ── Post Creation ──

data class PostRequest(
    val content: String,
    val type: Int? = 0, // 0 = Post
    val visibility: Int? = 0, // 0 = Public
)
