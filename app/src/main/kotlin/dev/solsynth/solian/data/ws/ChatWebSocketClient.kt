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
import org.json.JSONObject
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
    private val onMessage: (String, String?, String?) -> Unit,
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
    }

    fun connect() {
        disconnect()
        val wsUrl = buildWsUrl()
        val request = Request.Builder()
            .url(wsUrl)
            .addAuthHeader()
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                isConnected = true
                _statusFlow.value = true
                onStatusChanged(true)
                Log.d(TAG, "WebSocket connected: $wsUrl")
                startHeartbeat(ws)
                onReconnected()
            }

            override fun onMessage(ws: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    val type = json.optString("type")
                    val data = json.optJSONObject("data")

                    Log.d(TAG, "Received: $type")

                    when (type) {
                        "messages.new" -> {
                            val content = data?.optString("content")
                            val sender = data?.optJSONObject("sender")?.optString("name")
                            val chatRoomId = data?.optString("chat_room_id")
                            onMessage(content ?: "", sender, chatRoomId)
                        }
                        "messages.update" -> {
                            val messageId = data?.optString("id")
                            val content = data?.optString("content")
                            onMessage(content ?: "", null, messageId)
                        }
                        "messages.delete" -> {
                            val messageId = data?.optString("id")
                            onMessage("", null, messageId)
                        }
                        "messages.delivered" -> {
                            val messageId = data?.optString("id")
                            val content = data?.optString("content")
                            onMessage(content ?: "", null, messageId)
                        }
                        "messages.typing" -> {
                            val chatRoomId = data?.optString("chat_room_id")
                            onMessage("", null, chatRoomId)
                        }
                        "messages.reaction.added",
                        "messages.reaction.removed" -> {
                            val messageId = data?.optJSONObject("meta")?.optString("message_id")
                            onMessage("", null, messageId)
                        }
                        "pong" -> { /* heartbeat response */ }
                        else -> Log.d(TAG, "Unhandled packet: $type")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Parse error: $text", e)
                }
            }

            override fun onMessage(ws: WebSocket, bytes: ByteString) {
                onMessage(ws, bytes.utf8())
            }

            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                isConnected = false
                _statusFlow.value = false
                onStatusChanged(false)
                Log.d(TAG, "Closing: $code $reason")
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                isConnected = false
                _statusFlow.value = false
                onStatusChanged(false)
                Log.d(TAG, "Closed: $code $reason")
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
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
        return this
    }

    fun sendMessage(
        chatRoomId: String,
        content: String,
        repliedMessageId: String? = null,
        forwardedMessageId: String? = null,
        attachmentsId: List<String> = emptyList(),
        meta: JSONObject? = null,
    ) {
        val data = JSONObject()
            .put("chat_room_id", chatRoomId)
            .put("content", content)
            .putOpt("replied_message_id", repliedMessageId)
            .putOpt("forwarded_message_id", forwardedMessageId)
            .putOpt("attachments_id", JSONArray(attachmentsId))
            .putOpt("meta", meta)

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
