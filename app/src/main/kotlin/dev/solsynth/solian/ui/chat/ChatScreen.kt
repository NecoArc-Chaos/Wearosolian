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
import dev.solsynth.solian.data.ws.ChatWebSocketClient
import dev.solsynth.solian.theme.rememberIsScreenRound

@Composable
fun ChatScreen() {
    // Use mutableStateListOf so WebSocket callback sees latest value
    val rooms = mutableStateListOf<SnChatRoom>()
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var wsConnected by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberScalingLazyListState()
    val focusRequester = remember { FocusRequester() }
    val rotaryBehavior = RotaryScrollableDefaults.behavior(scrollableState = listState)
    val isRound = rememberIsScreenRound()

    // WebSocket client for real-time messages
    val wsClient = remember {
        ChatWebSocketClient(
            serverUrl = TokenStore.serverUrl,
            onMessage = { content, sender, _ ->
                if (content.isNotBlank() && rooms.isNotEmpty()) {
                    val firstRoom = rooms[0]
                    rooms[0] = firstRoom.copy(
                        lastMessage = "$sender: $content",
                    )
                }
            },
            onStatusChanged = { connected ->
                wsConnected = connected
            },
        )
    }

    // Initial load of chat rooms
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val resp = ApiClient.api.getChatRooms()
                rooms.clear()
                rooms.addAll(resp.rooms)
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }

    // Connect WebSocket when logged in
    LaunchedEffect(TokenStore.isLoggedIn) {
        if (TokenStore.isLoggedIn) {
            wsClient.connect()
        } else {
            wsClient.disconnect()
        }
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            wsClient.cleanup()
        }
    }

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
            Text("Messages", style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.fillMaxWidth(0.9f))
        }

        if (wsConnected) {
            item {
                Text("● Live", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary)
            }
        }

        if (isLoading && rooms.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else if (error != null && rooms.isEmpty()) {
            item {
                Text(error!!, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }
        } else if (rooms.isEmpty()) {
            item {
                Text("No chat rooms",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(rooms.size) { index ->
                val room = rooms[index]
                Card(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(0.9f).padding(vertical = 4.dp),
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        if (!room.name.isNullOrBlank()) {
                            Text(room.name, style = MaterialTheme.typography.labelSmall)
                        }
                        if (!room.lastMessage.isNullOrBlank()) {
                            Text(room.lastMessage,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}
