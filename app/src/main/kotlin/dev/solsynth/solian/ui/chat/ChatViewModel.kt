package dev.solsynth.solian.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.snapshotFlow
import dev.solsynth.solian.data.TokenStore
import dev.solsynth.solian.data.api.ApiClient
import dev.solsynth.solian.data.model.ChatSummaryEntry
import dev.solsynth.solian.data.ws.ChatWebSocketClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

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
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun connectWebSocket() {
        if (wsClient != null) return
        wsClient = ChatWebSocketClient(
            serverUrl = TokenStore.serverUrl,
            onMessage = { content, sender, chatRoomId ->
                if (content.isNotBlank() && _rooms.value.isNotEmpty()) {
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
                }
            },
            onStatusChanged = { connected ->
                _wsConnected.value = connected
            },
            onReconnected = {
                viewModelScope.launch {
                    try {
                        val summaryMap = ApiClient.api.getChatSummary()
                        _rooms.value = summaryMap.values.toList()
                    } catch (_: Exception) {
                        // Keep current rooms on sync failure
                    }
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
