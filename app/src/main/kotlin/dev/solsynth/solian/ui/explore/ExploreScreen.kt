package dev.solsynth.solian.ui.explore

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.*
import kotlinx.coroutines.launch
import dev.solsynth.solian.R
import dev.solsynth.solian.data.api.ApiClient
import dev.solsynth.solian.data.model.SnPost
import dev.solsynth.solian.ui.scaffold.WearScreen

@Composable
fun ExploreScreen() {
    var posts by remember { mutableStateOf<List<SnPost>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                posts = ApiClient.api.getTimeline()
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }

    WearScreen {
        if (isLoading) {
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else if (error != null) {
            item {
                Text(error!!, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }
        } else if (posts.isEmpty()) {
            item {
                Text(stringResource(R.string.explore_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(posts.size) { index ->
                val post = posts[index]
                if (post == null) return@items
                Card(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(0.9f).padding(vertical = 4.dp),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        if (!post.title.isNullOrBlank()) {
                            Text(post.title,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }
                        if (!post.content.isNullOrBlank()) {
                            Text(post.content,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}
