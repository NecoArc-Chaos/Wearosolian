package dev.solsynth.solian.data.model

import com.google.gson.annotations.SerializedName

// ── Auth ──
data class LoginRequest(
    val email: String,
    val password: String,
    @SerializedName("device_id") val deviceId: String = "wearos",
    @SerializedName("device_name") val deviceName: String = "Wear OS Watch",
)

data class LoginResponse(
    val token: String,
    @SerializedName("refresh_token") val refreshToken: String?,
    @SerializedName("expires_in") val expiresIn: Int?,
)

// ── Post / Timeline ──
data class SnPost(
    val id: String,
    val body: String?,
    val title: String?,
    @SerializedName("created_at") val createdAt: String?,
    val author: SnAuthor?,
    val attachments: List<SnAttachment>?,
)

data class SnAuthor(
    val id: String,
    val name: String,
    val avatar: String?,
)

data class SnAttachment(
    val id: String,
    val url: String?,
    val name: String?,
)

// ── Server Capabilities ──
data class ServerCapabilities(
    val version: String?,
    val features: List<String>?,
)
