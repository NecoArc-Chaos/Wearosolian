package dev.solsynth.solian.ui.chat

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.*
import androidx.wear.compose.material3.lazy.transformedHeight
import dev.solsynth.solian.R
import dev.solsynth.solian.data.TokenStore
import dev.solsynth.solian.ui.scaffold.WearScreen
import dev.solsynth.solian.util.RelativeTime

@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel()) {
    val rooms by viewModel.rooms.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val wsConnected by viewModel.wsConnected.collectAsState()

    val selectedRoom by viewModel.selectedRoom.collectAsState()

    if (selectedRoom != null) {
        ChatRoomView(viewModel = viewModel) { viewModel.closeRoom() }
    } else {
        WearScreen { spec ->
            item {
                ListHeader(
                    modifier = Modifier.transformedHeight(this, spec),
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
                        modifier = Modifier.padding(bottom = 8.dp).transformedHeight(this, spec),
                    )
                }
            }

            if (isLoading && rooms.isEmpty()) {
                item {
                    CircularProgressIndicator(
                        modifier = Modifier.size(ButtonDefaults.ExtraSmallIconSize).transformedHeight(this, spec),
                    )
                }
            } else if ((error != null) && rooms.isEmpty()) {
                item {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.transformedHeight(this, spec),
                    )
                }
            } else if (rooms.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.chat_empty),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.transformedHeight(this, spec),
                    )
                }
            } else {
                items(rooms.size) { index ->
                    val entry = rooms[index]
                    val room = entry.room ?: return@items
                    
                    AppCard(
                        onClick = { viewModel.openRoom(entry) },
                        appName = {
                            Text(
                                text = room.name ?: "Unnamed Chat",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        appImage = {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = (room.name?.take(1) ?: "?").uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        },
                        time = {
                            if (entry.lastMessage != null) {
                                Text(RelativeTime.format(room.updatedAt))
                            }
                        },
                        title = {
                            if (entry.unreadCount > 0) {
                                Text(
                                    text = "${entry.unreadCount} new",
                                    color = MaterialTheme.colorScheme.tertiary,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.94f).padding(vertical = 2.dp).transformedHeight(this, spec),
                        transformation = SurfaceTransformation(spec),
                    ) {
                        val lastContent = entry.lastMessage?.content
                        if (!lastContent.isNullOrBlank()) {
                            Text(
                                text = lastContent,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun ChatRoomView(
    viewModel: ChatViewModel,
    onBack: () -> Unit,
) {
    val entry by viewModel.selectedRoom.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isRoomLoading.collectAsState()
    val roomError by viewModel.roomError.collectAsState()
    val typingRooms by viewModel.typingRooms.collectAsState()
    val currentRoomId = entry?.room?.id

    BackHandler(onBack = onBack)

    WearScreen { spec ->
        item {
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.chat_back), style = MaterialTheme.typography.labelSmall)
            }
        }

        item {
            ListHeader(
                modifier = Modifier.transformedHeight(this, spec),
            ) {
                Text(entry?.room?.name ?: "Chat", textAlign = TextAlign.Center)
            }
        }

        if ((currentRoomId != null) && (currentRoomId in typingRooms)) {
            item {
                Text(
                    text = stringResource(R.string.chat_typing),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.transformedHeight(this, spec),
                )
            }
        }

        when {
            isLoading && messages.isEmpty() -> {
                item {
                    CircularProgressIndicator(
                        modifier = Modifier.size(ButtonDefaults.ExtraSmallIconSize).transformedHeight(this, spec),
                    )
                }
            }
            (roomError != null) && messages.isEmpty() -> {
                item {
                    Text(
                        text = roomError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.transformedHeight(this, spec),
                    )
                }
            }
            messages.isEmpty() -> {
                item {
                    Text(
                        text = stringResource(R.string.chat_room_empty),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.transformedHeight(this, spec),
                    )
                }
            }
            else -> {
                items(messages.size) { index ->
                    val message = messages[index]
                    val isMe = message.senderId == TokenStore.deviceId // Simplification
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(vertical = 4.dp)
                            .transformedHeight(this, spec),
                        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
                    ) {
                        if (!isMe) {
                            Text(
                                text = message.sender?.nick ?: message.sender?.name ?: "Unknown",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                        Card(
                            onClick = {},
                            colors = CardDefaults.cardColors(
                                containerColor = if (isMe) MaterialTheme.colorScheme.primaryContainer 
                                                else MaterialTheme.colorScheme.surfaceContainer,
                            ),
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            Text(
                                text = message.content ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(8.dp),
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}
