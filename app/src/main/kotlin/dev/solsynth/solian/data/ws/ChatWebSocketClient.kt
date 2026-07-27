package dev.solsynth.solian.data.ws

import android.util.Log
import dev.solsynth.solian.data.TokenStore
import okhttp3.*
import okio.ByteString
import org.json.JSONObject

/**
 * WebSocket client for real-time chat messages.
 *
 * Connects to the DysonNetwork WebSocket gateway and listens for
 * [WebSocketPacketType.MessageNew] events.
 *
 * Endpoint convention (needs server confirmation):
 *   wss://{server}/ws
 *
 * Authentication:
 *   Send {"token":"Bearer ..."} as first message after open.
 */
class ChatWebSocketClient(
    private val serverUrl: String,
    private val onMessage: (String, String?, String?) -> Unit,
    private val onStatusChanged: (Boolean) -> Unit,
) {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    private var isConnected = false

    fun connect() {
        val wsUrl = serverUrl.ensureWsScheme()
        val request = Request.Builder().url(wsUrl).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                isConnected = true
                onStatusChanged(true)
                Log.d(TAG, "WebSocket connected")

                // Authenticate
                val auth = JSONObject().apply {
                    put("token", "Bearer ${TokenStore.token}")
                }.toString()
                ws.send(auth)
            }

            override fun onMessage(ws: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    val type = json.optString("type")
                    val data = json.optJSONObject("data")

                    when (type) {
                        "message.new" -> {
                            val content = data?.optString("content")
                            val sender = data?.optJSONObject("sender")?.optString("name")
                            onMessage(content, sender, null)
                        }
                        "message.update" -> {
                            // Handle message update
                        }
                        "message.delete" -> {
                            // Handle message delete
                        }
                        else -> {
                            Log.d(TAG, "Unhandled packet: $type")
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to parse WS message: $text", e)
                }
            }

            override fun onMessage(ws: WebSocket, bytes: ByteString) {
                onMessage(ws, bytes.utf8())
            }

            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                isConnected = false
                onStatusChanged(false)
                Log.d(TAG, "WebSocket closing: $code / $reason")
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                isConnected = false
                onStatusChanged(false)
                Log.d(TAG, "WebSocket closed: $code / $reason")
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                isConnected = false
                onStatusChanged(false)
                Log.w(TAG, "WebSocket failure", t)
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "bye")
        webSocket = null
        isConnected = false
        onStatusChanged(false)
    }

    val isActive: Boolean get() = isConnected

    private fun String.ensureWsScheme(): String {
        val trimmed = this.trimEnd('/')
        return when {
            trimmed.startsWith("https://") -> "wss://${trimmed.substringAfter("https://")}/ws"
            trimmed.startsWith("http://") -> "ws://${trimmed.substringAfter("http://")}/ws"
            trimmed.startsWith("ws://") || trimmed.startsWith("wss://") -> trimmed
            else -> "wss://$trimmed/ws"
        }
    }

    companion object {
        private const val TAG = "ChatWebSocket"
    }
}
