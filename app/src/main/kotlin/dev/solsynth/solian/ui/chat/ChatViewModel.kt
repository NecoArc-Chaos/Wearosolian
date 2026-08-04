package dev.solsynth.solian.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.snapshotFlow
import android.util.Log
import dev.solsynth.solian.data.TokenStore
import dev.solsynth.solian.data.api.ApiClient
import dev.solsynth.solian.data.model.ChatSummaryEntry
import dev.solsynth.solian.data.model.ChatSyncRequest
import dev.solsynth.solian.data.model.SendMessageRequest
import dev.solsynth.solian.data.model.SnChatMessage
import dev.solsynth.solian.data.ws.ChatWsEvent
import dev.solsynth.solian.data.ws.ChatWebSocketClient
import kotlinx.coroutines.delay
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

    private val _selectedRoom = MutableStateFlow<ChatSummaryEntry?>(null)
    val selectedRoom: StateFlow<ChatSummaryEntry?> = _selectedRoom

    private val _messages = MutableStateFlow<List<SnChatMessage>>(emptyList())
    val messages: StateFlow<List<SnChatMessage>> = _messages

    private val _isRoomLoading = MutableStateFlow(false)
    val isRoomLoading: StateFlow<Boolean> = _isRoomLoading

    private val _roomError = MutableStateFlow<String?>(null)
    val roomError: StateFlow<String?> = _roomError

    private val _typingRooms = MutableStateFlow<Set<String>>(emptySet())
    val typingRooms: StateFlow<Set<String>> = _typingRooms

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
                val rooms = ApiClient.api.getRooms(take = 50)
                val sync = ApiClient.api.syncRooms(ChatSyncRequest(lastSyncTimestamp = 0))
                val summaries = sync.summaries ?: emptyMap()
                _rooms.value = rooms.map { room ->
                    summaries[room.id]?.copy(room = room) ?: ChatSummaryEntry(room = room)
                }
                lastSyncTimestamp = sync.currentTimestamp ?: System.currentTimeMillis()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun openRoom(entry: ChatSummaryEntry) {
        val roomId = entry.room?.id ?: return
        _selectedRoom.value = entry
        _roomError.value = null
        viewModelScope.launch {
            _isRoomLoading.value = true
            try {
                val history = ApiClient.api.getMessages(roomId, take = 50)
                _messages.value = history
            } catch (e: Exception) {
                _roomError.value = e.message
            } finally {
                _isRoomLoading.value = false
            }
        }
    }

    fun closeRoom() {
        _selectedRoom.value = null
        _messages.value = emptyList()
        _roomError.value = null
    }

    fun sendText(text: String) {
        val roomId = _selectedRoom.value?.room?.id ?: return
        viewModelScope.launch {
            try {
                val sent = ApiClient.api.sendMessage(roomId, SendMessageRequest(content = text))
                upsertMessage(sent)
            } catch (e: Exception) {
                _roomError.value = e.message
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
            onEvent = { event ->
                when (event) {
                    is ChatWsEvent.NewMessage -> handleNewMessage(event.message)
                    is ChatWsEvent.UpdateMessage -> handleUpdateMessage(event)
                    is ChatWsEvent.DeleteMessage -> handleDeleteMessage(event)
                    is ChatWsEvent.Delivered -> handleDelivered(event.message)
                    is ChatWsEvent.Typing -> handleTyping(event)
                    is ChatWsEvent.ReactionUpdated -> handleReactionUpdated(event)
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

    private fun handleNewMessage(message: SnChatMessage) {
        val current = _rooms.value.toMutableList()
        val roomId = message.chatRoomId ?: return
        val roomIndex = current.indexOfFirst { entry ->
            entry.room?.id == roomId
        }
        if (roomIndex >= 0) {
            val entry = current[roomIndex]
            val shouldUpdate = entry.lastMessage == null ||
                (message.roomSequence ?: 0) > (entry.lastMessage.roomSequence ?: 0)
            if (shouldUpdate) {
                current[roomIndex] = entry.copy(lastMessage = message)
                _rooms.value = current
            }
        }
        if (roomId == _selectedRoom.value?.room?.id) {
            upsertMessage(message)
        }
    }

    private fun upsertMessage(message: SnChatMessage) {
        val current = _messages.value
        if (current.any { it.id == message.id }) {
            _messages.value = current.map { if (it.id == message.id) message else it }
        } else {
            _messages.value = current + message
        }
    }

    private fun handleUpdateMessage(event: ChatWsEvent.UpdateMessage) {
        val roomId = event.roomId
        if (roomId != null && roomId == _selectedRoom.value?.room?.id) {
            _messages.value = _messages.value.map { message ->
                if (message.id == event.messageId) {
                    message.copy(content = event.content ?: message.content)
                } else {
                    message
                }
            }
        }
        val current = _rooms.value.toMutableList()
        val roomIndex = current.indexOfFirst { entry ->
            entry.room?.id == roomId
        }
        if (roomIndex >= 0) {
            val entry = current[roomIndex]
            val lastMessage = entry.lastMessage
            val updatedLastMessage = lastMessage?.takeIf { it.id == event.messageId }
                ?.copy(content = event.content ?: lastMessage.content)
            if (updatedLastMessage != null) {
                current[roomIndex] = entry.copy(lastMessage = updatedLastMessage)
                _rooms.value = current
            }
        }
    }

    private fun handleDeleteMessage(event: ChatWsEvent.DeleteMessage) {
        val roomId = event.roomId
        if (roomId != null && roomId == _selectedRoom.value?.room?.id) {
            _messages.value = _messages.value.filterNot { it.id == event.messageId }
        }
        val current = _rooms.value.toMutableList()
        val roomIndex = current.indexOfFirst { entry ->
            entry.room?.id == roomId
        }
        if (roomIndex >= 0) {
            val entry = current[roomIndex]
            if (entry.lastMessage?.id == event.messageId) {
                current[roomIndex] = entry.copy(lastMessage = null)
                _rooms.value = current
            }
        }
    }

    private fun handleDelivered(message: SnChatMessage) {
        val current = _rooms.value.toMutableList()
        val roomId = message.chatRoomId ?: return
        val roomIndex = current.indexOfFirst { entry ->
            entry.room?.id == roomId
        }
        if (roomIndex >= 0) {
            val entry = current[roomIndex]
            if (entry.lastMessage?.id == message.id) {
                current[roomIndex] = entry.copy(lastMessage = message)
                _rooms.value = current
            }
        }
        if (roomId == _selectedRoom.value?.room?.id) {
            upsertMessage(message)
        }
    }

    private fun handleTyping(event: ChatWsEvent.Typing) {
        _typingRooms.value = _typingRooms.value + event.roomId
        viewModelScope.launch {
            delay(3_000)
            _typingRooms.value = _typingRooms.value - event.roomId
        }
    }

    private fun handleReactionUpdated(event: ChatWsEvent.ReactionUpdated) {
        val current = _rooms.value.toMutableList()
        for (i in current.indices) {
            val entry = current[i]
            if (entry.lastMessage?.id == event.messageId) {
                current[i] = entry.copy(
                    lastMessage = entry.lastMessage.copy(
                        reactions_count = event.reactionsCount,
                    )
                )
                _rooms.value = current
                break
            }
        }
        if (_messages.value.any { it.id == event.messageId }) {
            _messages.value = _messages.value.map { message ->
                if (message.id == event.messageId) {
                    message.copy(reactions_count = event.reactionsCount)
                } else {
                    message
                }
            }
        }
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
