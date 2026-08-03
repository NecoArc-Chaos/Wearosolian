package dev.solsynth.solian.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.*
import androidx.wear.compose.material3.lazy.transformedHeight
import dev.solsynth.solian.R
import dev.solsynth.solian.ui.scaffold.WearScreen
import dev.solsynth.solian.util.RelativeTime

@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel()) {
    val rooms by viewModel.rooms.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val wsConnected by viewModel.wsConnected.collectAsState()

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
                
                AppCard(
                    onClick = { /* TODO: Open Room */ },
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
                        if (entry.unreadCount > 0) {
                            Text(
                                text = "${entry.unreadCount} new",
                                color = MaterialTheme.colorScheme.tertiary,
                                style = MaterialTheme.typography.labelSmall
                            )
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
