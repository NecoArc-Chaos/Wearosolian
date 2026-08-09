package dev.solsynth.solian.data.ws

import android.util.Log
import dev.solsynth.solian.data.NetworkConfig
import dev.solsynth.solian.data.TokenStore
import dev.solsynth.solian.data.api.ApiClient
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.*
import okio.ByteString
import org.json.JSONArray
import org.json.JSONObject
import dev.solsynth.solian.data.ws.ChatWsEvent
import dev.solsynth.solian.data.model.SnChatMessage
import dev.solsynth.solian.data.model.SnChatMember
import java.util.concurrent.TimeUnit

/**
 * WebSocket client for real-time chat messages.
 *
 * Matches DysonNetwork client protocol (v3 gateway):
 * - Endpoint: {serverUrl}/ws (http→ws, https→wss)
 * - Auth: Authorization: Bearer {token} header
 * - Packet envelope: { type, data, endpoint, error_message? }
 * - Client outbound endpoint: "DysonNetwork.Messager"
 * - Packet types: messages.send, messages.typing, ping/pong
 * - Server inbound: messages.new, messages.update, messages.delete,
 *                   messages.delivered, messages.typing,
 *                   messages.reaction.added, messages.reaction.removed
 * - Heartbeat: client sends ping every 30s, server responds pong
 */
class ChatWebSocketClient(
    private val serverUrl: String,
    private val onEvent: (ChatWsEvent) -> Unit,
    private val onStatusChanged: (Boolean) -> Unit,
    private val onReconnected: () -> Unit = {},
) {
    // Reuse the shared OkHttpClient from ApiClient so that connection pools,
    // DNS cache, and certificate pinning are shared with the REST client.
    private val client = ApiClient.httpClient.newBuilder()
        .pingInterval(30, TimeUnit.SECONDS)
        .build()
    private var webSocket: WebSocket? = null
    private var isConnected = false
    private var heartbeatJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _statusFlow = MutableStateFlow(false)
    val statusFlow: StateFlow<Boolean> = _statusFlow

    companion object {
        private const val MESSENGER_ENDPOINT = "DysonNetwork.Messager"
        private const val TAG = "ChatWebSocket"
        private const val MLS_CLIENT_ABILITY = "chat.mls.v2"
    }

    fun connect() {
        disconnect()
        val wsUrl = buildWsUrl()
        val request = Request.Builder()
            .url(wsUrl)
            .addAuthHeader()
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnected = true
                _statusFlow.value = true
                onStatusChanged(true)
                Log.d(TAG, "WebSocket connected: $wsUrl")
                startHeartbeat(webSocket)
                onReconnected()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    val type = json.optString("type")
                    val data = json.optJSONObject("data")

                    Log.d(TAG, "Received: $type")

                    when (type) {
                        "messages.new" -> {
                            val message = parseChatMessage(data)
                            if (message != null) {
                                onEvent(ChatWsEvent.NewMessage(message))
                            }
                        }
                        "messages.update" -> {
                            val messageId = data?.optString("id")
                            val chatRoomId = data?.optString("chat_room_id")
                            val content = data?.optString("content")
                            if (messageId != null) {
                                onEvent(ChatWsEvent.UpdateMessage(messageId, chatRoomId, content))
                            }
                        }
                        "messages.delete" -> {
                            val messageId = data?.optString("id")
                            val chatRoomId = data?.optString("chat_room_id")
                            if (messageId != null) {
                                onEvent(ChatWsEvent.DeleteMessage(messageId, chatRoomId))
                            }
                        }
                        "messages.delivered" -> {
                            val message = parseChatMessage(data)
                            if (message != null) {
                                onEvent(ChatWsEvent.Delivered(message))
                            }
                        }
                        "messages.typing" -> {
                            val chatRoomId = data?.optString("chat_room_id")
                            val typingType = data?.optString("type") ?: "typing"
                            if (chatRoomId != null) {
                                onEvent(ChatWsEvent.Typing(chatRoomId, typingType))
                            }
                        }
                        "messages.reaction.added",
                        "messages.reaction.removed" -> {
                            // Reaction packets carry meta either wrapped in `data` or at the
                            // top level of the envelope (kb developer docs show the latter).
                            val meta = data?.optJSONObject("meta") ?: json.optJSONObject("meta")
                            val messageId = meta?.optString("message_id")
                            val symbol = meta?.optString("symbol")
                            val reactionsCount = parseReactionsCount(meta?.optJSONObject("reactions_count"))
                            if (messageId != null) {
                                onEvent(ChatWsEvent.ReactionUpdated(messageId, symbol, reactionsCount))
                            }
                        }
                        "pong" -> { /* heartbeat response */ }
                        else -> Log.d(TAG, "Unhandled packet: $type")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Parse error: $text", e)
                }
            }

            private fun parseChatMessage(data: JSONObject?): SnChatMessage? {
                return try {
                    SnChatMessage(
                        id = data?.optString("id") ?: return null,
                        content = if (data.has("content")) data.optString("content") else null,
                        type = if (data.has("type")) data.optString("type") else null,
                        senderId = if (data.has("sender_id")) data.optString("sender_id") else null,
                        chatRoomId = if (data.has("chat_room_id")) data.optString("chat_room_id") else null,
                        sender = data.optJSONObject("sender")?.let { senderJson ->
                            SnChatMember(
                                id = senderJson.optString("id"),
                                name = if (senderJson.has("name")) senderJson.optString("name") else null,
                                nick = if (senderJson.has("nick")) senderJson.optString("nick") else null,
                            )
                        },
                        roomSequence = data.optLong("room_sequence").takeIf { it > 0 },
                        createdAt = if (data.has("created_at")) data.optString("created_at") else null,
                        reactionsCount = parseReactionsCount(data.optJSONObject("reactions_count")),
                        reactionsMade = parseReactionsMade(data.optJSONObject("reactions_made")),
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to parse chat message", e)
                    null
                }
            }

            private fun parseReactionsCount(json: JSONObject?): Map<String, Int>? {
                if (json == null) return null
                val result = mutableMapOf<String, Int>()
                val keys = json.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    result[key] = json.optInt(key)
                }
                return result.ifEmpty { null }
            }

            private fun parseReactionsMade(json: JSONObject?): Map<String, Boolean>? {
                if (json == null) return null
                val result = mutableMapOf<String, Boolean>()
                val keys = json.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    result[key] = json.optBoolean(key, false)
                }
                return result.ifEmpty { null }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                onMessage(webSocket, bytes.utf8())
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
                _statusFlow.value = false
                onStatusChanged(false)
                Log.d(TAG, "Closing: $code $reason")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
                _statusFlow.value = false
                onStatusChanged(false)
                Log.d(TAG, "Closed: $code $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isConnected = false
                _statusFlow.value = false
                onStatusChanged(false)
                Log.w(TAG, "Failure: ${t.message}", t)
                scheduleReconnect()
            }
        })
    }

    fun disconnect() {
        heartbeatJob?.cancel()
        webSocket?.close(1000, "bye")
        webSocket = null
        isConnected = false
        _statusFlow.value = false
        onStatusChanged(false)
    }

    val isActive: Boolean get() = isConnected

    private fun startHeartbeat(ws: WebSocket) {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            while (webSocket != null) {
                delay(30_000)
                try {
                    val ping = JSONObject()
                        .put("type", "ping")
                        .put("endpoint", MESSENGER_ENDPOINT)
                        .toString()
                    ws.send(ping)
                } catch (e: Exception) {
                    Log.w(TAG, "Heartbeat failed", e)
                    break
                }
            }
        }
    }

    private var reconnectJob: Job? = null
    private fun scheduleReconnect() {
        if (reconnectJob?.isActive == true) return
        reconnectJob = scope.launch {
            delay(5_000)
            if (!isConnected && TokenStore.isLoggedIn) {
                Log.d(TAG, "Reconnecting...")
                connect()
            }
        }
    }

    private fun buildWsUrl(): String {
        val trimmed = serverUrl.trimEnd('/')
        val host = when {
            trimmed.startsWith("https://") -> trimmed.substringAfter("https://")
            trimmed.startsWith("http://") -> trimmed.substringAfter("http://")
            else -> trimmed
        }
        val scheme = if (trimmed.startsWith("https://")) "wss" else "ws"
        return "$scheme://$host/ws"
    }

    private fun Request.Builder.addAuthHeader(): Request.Builder {
        val token = TokenStore.token
        if (!token.isNullOrBlank()) {
            header("Authorization", "Bearer $token")
        }
        header("X-Client-Ability", MLS_CLIENT_ABILITY)
        return this
    }

    fun sendMessage(
        chatRoomId: String,
        content: String,
        repliedMessageId: String? = null,
        forwardedMessageId: String? = null,
        attachmentsId: List<String> = emptyList(),
        meta: JSONObject? = null,
        encryptionMode: Int = 0,
    ) {
        val data = JSONObject()
            .put("chat_room_id", chatRoomId)
            .put("content", content)
            .putOpt("replied_message_id", repliedMessageId)
            .putOpt("forwarded_message_id", forwardedMessageId)
            .putOpt("attachments_id", JSONArray(attachmentsId))
            .putOpt("meta", meta)

        if (encryptionMode == 3) {
            data.put("encryption_scheme", MLS_CLIENT_ABILITY)
        }

        val payload = JSONObject()
            .put("type", "messages.send")
            .put("endpoint", MESSENGER_ENDPOINT)
            .put("data", data)
            .toString()

        webSocket?.send(payload)
    }

    fun sendTypingIndicator(chatRoomId: String, type: String = "typing") {
        val payload = JSONObject()
            .put("type", "messages.typing")
            .put("endpoint", MESSENGER_ENDPOINT)
            .put("data", JSONObject()
                .put("chat_room_id", chatRoomId)
                .put("type", type)
            )
            .toString()
        webSocket?.send(payload)
    }

    fun cleanup() {
        heartbeatJob?.cancel()
        reconnectJob?.cancel()
        scope.cancel()
        disconnect()
    }
}
