package dev.solsynth.solian.ui.chat

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material3.OutlinedTextField
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.*
import androidx.wear.compose.material3.lazy.transformedHeight
import dev.solsynth.solian.R
import dev.solsynth.solian.data.model.SnChatMessage
import dev.solsynth.solian.ui.scaffold.WearScreen
import dev.solsynth.solian.util.RelativeTime

@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel()) {
    val selectedRoom by viewModel.selectedRoom.collectAsState()
    val roomId = selectedRoom?.room?.id

    BackHandler(enabled = roomId != null) {
        viewModel.closeRoom()
    }

    if (roomId != null) {
        ChatRoomView(viewModel = viewModel, onBack = { viewModel.closeRoom() })
    } else {
        ChatRoomListView(viewModel = viewModel)
    }
}

@Composable
private fun ChatRoomListView(viewModel: ChatViewModel) {
    val rooms by viewModel.rooms.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val wsConnected by viewModel.wsConnected.collectAsState()
    val typingRooms by viewModel.typingRooms.collectAsState()

    WearScreen { spec ->
        item {
            ListHeader(
                modifier = Modifier.transformedHeight(this, spec)
            ) {
                Text(stringResource(R.string.chat_title))
            }
        }

        if (wsConnected) {
            item {
                Text(
                    text = stringResource(R.string.chat_live),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp).transformedHeight(this, spec)
                )
            }
        }

        if (isLoading && rooms.isEmpty()) {
            item {
                CircularProgressIndicator(
                    modifier = Modifier.size(ButtonDefaults.ExtraSmallIconSize).transformedHeight(this, spec)
                )
            }
        } else if (error != null && rooms.isEmpty()) {
            item {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.transformedHeight(this, spec)
                )
            }
        } else if (rooms.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.chat_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.transformedHeight(this, spec)
                )
            }
        } else {
            items(rooms.size) { index ->
                val entry = rooms[index]
                val room = entry.room ?: return@items
                val isTyping = room.id in typingRooms

                AppCard(
                    onClick = { viewModel.openRoom(entry) },
                    appName = {
                        Text(
                            text = room.name ?: "Unnamed Chat",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    appImage = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (room.name?.take(1) ?: "?").uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    },
                    time = {
                        if (entry.lastMessage != null) {
                            Text(RelativeTime.format(room.updatedAt))
                        }
                    },
                    title = {
                        when {
                            entry.unreadCount > 0 -> {
                                Text(
                                    text = "${entry.unreadCount} new",
                                    color = MaterialTheme.colorScheme.tertiary,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            isTyping -> {
                                Text(
                                    text = stringResource(R.string.chat_typing),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.94f).padding(vertical = 2.dp).transformedHeight(this, spec),
                    transformation = SurfaceTransformation(spec)
                ) {
                    val lastContent = entry.lastMessage?.content
                    if (!lastContent.isNullOrBlank()) {
                        Text(
                            text = lastContent,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun ChatRoomView(
    viewModel: ChatViewModel,
    onBack: () -> Unit,
) {
    val room by viewModel.selectedRoom.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isRoomLoading.collectAsState()
    val roomError by viewModel.roomError.collectAsState()
    val typingRooms by viewModel.typingRooms.collectAsState()
    val currentRoomId = room?.room?.id

    var draft by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.chat_back))
            }
            Text(
                text = room?.room?.name ?: "Chat",
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            when {
                isLoading && messages.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center).size(24.dp)
                    )
                }
                roomError != null && messages.isEmpty() -> {
                    Text(
                        text = roomError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    )
                }
                messages.isEmpty() -> {
                    Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        if (currentRoomId != null && currentRoomId in typingRooms) {
                            Text(
                                text = stringResource(R.string.chat_typing),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.chat_room_empty),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    ) {
                        items(messages.size) { index ->
                            MessageBubble(message = messages[index])
                        }
                        item {
                            if (currentRoomId != null && currentRoomId in typingRooms) {
                                Text(
                                    text = stringResource(R.string.chat_typing),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                placeholder = { Text(stringResource(R.string.chat_input_hint)) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium,
            )
            Spacer(Modifier.width(6.dp))
            Button(
                onClick = {
                    val text = draft.trim()
                    if (text.isNotEmpty()) {
                        viewModel.sendText(text)
                        draft = ""
                    }
                },
                enabled = draft.isNotBlank(),
                modifier = Modifier.padding(start = 4.dp),
            ) {
                Text(stringResource(R.string.chat_send))
            }
        }
    }
}

@Composable
private fun MessageBubble(message: SnChatMessage) {
    val senderName = message.sender?.name ?: message.sender?.nick ?: message.senderId ?: "?"
    val time = RelativeTime.format(message.createdAt)

    Card(
        onClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = senderName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (time.isNotEmpty()) {
                    Text(
                        text = time,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = message.content ?: "",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
