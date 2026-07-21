package dev.solsynth.solian.data.model

import com.google.gson.annotations.SerializedName

// ── Auth ──

data class ChallengeRequest(
    val account: String,
    // Backend ClientPlatform enum: 0=Unidentified, 1=Web, 2=Ios, 3=Android, 4=MacOs...
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
    val type: Int,         // 0=Password, 1=EmailCode, 2=InAppCode, 3=TimedCode(TOTP), 4=PinCode, ...
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
)

// ── Post ──

data class SnPost(
    val id: String,
    val content: String?,
    val author: SnAuthor?,
    @SerializedName("created_at") val createdAt: String?,
)

data class SnAuthor(
    val name: String,
    val avatar: String?,
)

data class PostRequest(
    val content: String,
    val type: Int? = 0,
    val visibility: Int? = 0,
)
