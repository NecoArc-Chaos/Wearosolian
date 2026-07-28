package dev.solsynth.solian.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material3.*
import kotlinx.coroutines.launch
import dev.solsynth.solian.data.TokenStore
import dev.solsynth.solian.data.api.ApiClient
import dev.solsynth.solian.data.model.SnChatRoom
import dev.solsynth.solian.data.model.ChatSummaryEntry
import dev.solsynth.solian.data.ws.ChatWebSocketClient
import dev.solsynth.solian.theme.rememberIsScreenRound

@Composable
fun ChatScreen() {
    // roomId → summary (includes room info + lastMessage + unreadCount)
    val summaries = mutableStateMapOf<String, ChatSummaryEntry>()
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var wsConnected by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberScalingLazyListState()
    val focusRequester = remember { FocusRequester() }
    val rotaryBehavior = RotaryScrollableDefaults.behavior(scrollableState = listState)
    val isRound = rememberIsScreenRound()

    // WebSocket client — matches messages.new by chatRoomId
    val wsClient = remember {
        ChatWebSocketClient(
            serverUrl = TokenStore.serverUrl,
            onMessage = { content, sender, chatRoomId ->
                if (content.isNotBlank() && chatRoomId != null) {
                    val existing = summaries[chatRoomId]
                    if (existing != null) {
                        summaries[chatRoomId] = existing.copy(
                            lastMessage = existing.lastMessage?.copy(content = content),
                            unreadCount = existing.unreadCount + 1,
                        )
                    }
                }
            },
            onStatusChanged = { wsConnected = it },
        )
    }

    // Load chat summary on first composition
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val data = ApiClient.api.getChatSummary()
                summaries.clear()
                summaries.putAll(data)
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }

    // Connect WebSocket when logged in
    LaunchedEffect(TokenStore.isLoggedIn) {
        if (TokenStore.isLoggedIn) wsClient.connect() else wsClient.disconnect()
    }

    DisposableEffect(Unit) {
        onDispose { wsClient.cleanup() }
    }

    val sortedRooms = summaries.entries
        .sortedByDescending { it.value.lastMessage?.createdAt ?: "" }

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .rotaryScrollable(rotaryBehavior, focusRequester),
        state = listState,
        contentPadding = PaddingValues(
            top = if (isRound) 36.dp else 8.dp,
            bottom = if (isRound) 36.dp else 16.dp,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item { Spacer(Modifier.height(8.dp)) }
        item {
            Row(Modifier.fillMaxWidth(0.9f), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Messages", style = MaterialTheme.typography.titleSmall)
                if (wsConnected) {
                    Text("● Live", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        when {
            isLoading -> item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            error != null -> item {
                Text(error!!, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }
            sortedRooms.isEmpty() -> item {
                Text("No chat rooms", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> items(sortedRooms.size) { index ->
                val (roomId, entry) = sortedRooms[index]
                val room = entry.room
                val lastMsg = entry.lastMessage
                Card(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(0.9f).padding(vertical = 4.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            val displayName = room?.name
                                ?: lastMsg?.sender?.nick
                                ?: lastMsg?.sender?.name
                                ?: "Chat"
                            Text(displayName, style = MaterialTheme.typography.labelSmall,
                                maxLines = 1)
                            if (lastMsg?.content != null) {
                                Text(lastMsg.content, style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        if (entry.unreadCount > 0) {
                            Text("${entry.unreadCount}", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}
