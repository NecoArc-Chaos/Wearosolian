package dev.solsynth.solian.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.*
import dev.solsynth.solian.R
import dev.solsynth.solian.ui.scaffold.WearScreen

@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel()) {
    val rooms by viewModel.rooms.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val wsConnected by viewModel.wsConnected.collectAsState()

    WearScreen {
        item { Spacer(Modifier.height(8.dp)) }

        item {
            Text(stringResource(R.string.chat_title), style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.fillMaxWidth(0.9f))
        }

        if (wsConnected) {
            item {
                Text(stringResource(R.string.chat_live), style = MaterialTheme.typography.labelSmall,
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
            val errorMessage = error
            item {
                Text(errorMessage, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }
        } else if (rooms.isEmpty()) {
            item {
                Text(stringResource(R.string.chat_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(rooms.size) { index ->
                val entry = rooms[index]
                val room = entry.room
                if (room == null) return@items
                Card(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(0.9f).padding(vertical = 4.dp),
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            if (!room.name.isNullOrBlank()) {
                                Text(room.name, style = MaterialTheme.typography.labelSmall)
                            }
                            if (entry.unreadCount > 0) {
                                Text(
                                    text = "${entry.unreadCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }
                        if (!entry.lastMessage?.content.isNullOrBlank()) {
                            Text(
                                entry.lastMessage?.content.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}
