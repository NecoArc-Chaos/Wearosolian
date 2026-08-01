package dev.solsynth.solian.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.snapshotFlow
import dev.solsynth.solian.data.TokenStore
import dev.solsynth.solian.data.api.ApiClient
import dev.solsynth.solian.data.model.ChatSummaryEntry
import dev.solsynth.solian.data.model.ChatSyncRequest
import dev.solsynth.solian.data.model.RoomMessageSyncResponse
import dev.solsynth.solian.data.model.RoomSyncResponse
import dev.solsynth.solian.data.model.SnChatMessage
import dev.solsynth.solian.data.ws.ChatWebSocketClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.json.JSONObject

class ChatViewModel : ViewModel() {
    private val _rooms = MutableStateFlow<List<ChatSummaryEntry>>(emptyList())
    val rooms: StateFlow<List<ChatSummaryEntry>> = _rooms

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _wsConnected = MutableStateFlow(false)
    val wsConnected: StateFlow<Boolean> = _wsConnected

    private var wsClient: ChatWebSocketClient? = null
    private var lastSyncTimestamp = 0L

    init {
        loadRooms()
        observeLoginState()
    }

    private fun observeLoginState() {
        viewModelScope.launch {
            snapshotFlow { TokenStore.isLoggedIn }
                .distinctUntilChanged()
                .collectLatest { loggedIn ->
                    if (loggedIn) {
                        connectWebSocket()
                    } else {
                        disconnectWebSocket()
                    }
                }
        }
    }

    fun loadRooms() {
        viewModelScope.launch {
            try {
                val summaryMap = ApiClient.api.getChatSummary()
                _rooms.value = summaryMap.values.toList()
                lastSyncTimestamp = System.currentTimeMillis()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun syncChat() {
        try {
            val request = ChatSyncRequest(lastSyncTimestamp = lastSyncTimestamp)
            val response = ApiClient.api.syncAll(request)
            response.messages?.let { messages ->
                mergeMessages(messages)
            }
            response.currentTimestamp?.let { timestamp ->
                lastSyncTimestamp = timestamp
            }
        } catch (_: Exception) {
            // Keep current rooms on sync failure
        }
    }

    private fun mergeMessages(messages: List<SnChatMessage>) {
        val current = _rooms.value.toMutableList()
        for (message in messages) {
            val roomId = message.chatRoomId ?: continue
            val roomIndex = current.indexOfFirst { entry ->
                entry.room?.id == roomId
            }
            if (roomIndex >= 0) {
                val entry = current[roomIndex]
                val shouldUpdate = entry.lastMessage == null ||
                    (message.roomSequence ?: 0) > (entry.lastMessage.roomSequence ?: 0)
                if (shouldUpdate) {
                    current[roomIndex] = entry.copy(lastMessage = message)
                }
            }
        }
        _rooms.value = current
    }

    fun connectWebSocket() {
        if (wsClient != null) return
        wsClient = ChatWebSocketClient(
            serverUrl = TokenStore.serverUrl,
            onChatMessage = { content, sender, chatRoomId, messageId, reactionsCount ->
                if (content.isNotBlank() && chatRoomId != null && _rooms.value.isNotEmpty()) {
                    val current = _rooms.value.toMutableList()
                    val roomIndex = current.indexOfFirst { entry ->
                        entry.room?.id == chatRoomId
                    }
                    if (roomIndex >= 0) {
                        val entry = current[roomIndex]
                        current[roomIndex] = entry.copy(
                            lastMessage = entry.lastMessage?.copy(content = "$sender: $content")
                        )
                        _rooms.value = current
                    }
                } else if (messageId != null && content.isBlank() && reactionsCount != null) {
                    val current = _rooms.value.toMutableList()
                    val roomIndex = current.indexOfFirst { entry ->
                        entry.lastMessage?.id == messageId
                    }
                    if (roomIndex >= 0) {
                        val entry = current[roomIndex]
                        val reactionMap = mutableMapOf<String, Int>()
                        val keys = reactionsCount.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            reactionMap[key] = reactionsCount.optInt(key)
                        }
                        current[roomIndex] = entry.copy(
                            lastMessage = entry.lastMessage?.copy(
                                reactions_count = reactionMap,
                            )
                        )
                        _rooms.value = current
                    }
                }
            },
            onStatusChanged = { connected ->
                _wsConnected.value = connected
            },
            onReconnected = {
                viewModelScope.launch {
                    syncChat()
                }
            },
        )
        wsClient?.connect()
    }

    fun disconnectWebSocket() {
        wsClient?.disconnect()
        wsClient = null
        _wsConnected.value = false
    }

    override fun onCleared() {
        super.onCleared()
        wsClient?.cleanup()
        wsClient = null
    }
}
