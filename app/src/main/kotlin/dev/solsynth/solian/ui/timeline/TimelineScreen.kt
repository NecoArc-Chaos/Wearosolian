package dev.solsynth.solian.ui.timeline

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material3.*
import kotlinx.coroutines.launch
import dev.solsynth.solian.data.TokenStore
import dev.solsynth.solian.data.api.ApiClient
import dev.solsynth.solian.data.model.SnPost

@Composable
fun TimelineScreen(
    onNavigateToChat: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    var posts by remember { mutableStateOf<List<SnPost>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val listState = rememberScalingLazyListState()
    val rotaryBehavior = RotaryScrollableDefaults.behavior(scrollableState = listState)

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val token = "Bearer ${TokenStore.token}"
                posts = ApiClient.api.getTimeline(token)
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .rotaryScrollable(rotaryBehavior, focusRequester),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item { Spacer(Modifier.height(8.dp)) }

        if (isLoading) {
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else if (error != null) {
            item {
                Text(
                    error!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = onLogout) {
                    Text("Logout")
                }
            }
        } else if (posts.isEmpty()) {
            item {
                Text(
                    "No posts yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(posts.size) { index ->
                PostCard(posts[index])
            }
        }

        // Navigation
        item { Spacer(Modifier.height(8.dp)) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(0.8f),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Button(
                    onClick = onNavigateToChat,
                    modifier = Modifier.size(ButtonDefaults.SmallButtonSize),
                ) { Text("Chat") }
                Button(
                    onClick = onLogout,
                    modifier = Modifier.size(ButtonDefaults.SmallButtonSize),
                ) { Text("Exit") }
            }
        }
    }
}

@Composable
private fun PostCard(post: SnPost) {
    Card(
        modifier = Modifier.fillMaxWidth(0.9f).padding(vertical = 4.dp),
        onClick = {},
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            if (post.author != null) {
                Text(
                    post.author.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (!post.body.isNullOrBlank()) {
                Text(
                    post.body,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
