package dev.solsynth.solian.ui.explore

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.material3.*
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import coil.compose.AsyncImage
import dev.solsynth.solian.R
import dev.solsynth.solian.data.api.ApiClient
import dev.solsynth.solian.data.model.SnAttachment
import dev.solsynth.solian.data.model.SnPost
import dev.solsynth.solian.ui.scaffold.WearScreen
import dev.solsynth.solian.util.RelativeTime
import kotlinx.coroutines.launch
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState

@Composable
fun ExploreScreen(viewModel: ExploreViewModel = viewModel()) {
    val posts by viewModel.posts.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val error by viewModel.error.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()

    val selectedPostStack = remember { mutableStateListOf<SnPost>() }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = selectedPostStack.lastOrNull(),
            transitionSpec = {
                if (targetState != null) {
                    (slideInHorizontally(animationSpec = tween(300)) { it } + fadeIn()) togetherWith
                            (slideOutHorizontally(animationSpec = tween(300)) { -it / 2 } + fadeOut())
                } else {
                    (slideInHorizontally(animationSpec = tween(300)) { -it / 2 } + fadeIn()) togetherWith
                            (slideOutHorizontally(animationSpec = tween(300)) { it } + fadeOut())
                }
            },
            label = "ExploreNavigation",
            modifier = Modifier.fillMaxSize(),
        ) { targetPost ->
            if (targetPost != null) {
                PostDetailScreen(
                    post = targetPost,
                    onBack = { selectedPostStack.removeAt(selectedPostStack.size - 1) },
                ) {
                    selectedPostStack.add(it)
                }
            } else {
                WearScreen(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    pullToRefreshState = pullToRefreshState,
                    edgeButton = {
                        EdgeButton(
                            onClick = { viewModel.loadMore() },
                            enabled = !isLoadingMore,
                            buttonSize = EdgeButtonSize.Large,
                        ) {
                            if (isLoadingMore) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(ButtonDefaults.ExtraSmallIconSize),
                                )
                            } else {
                                Text("Load More", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    },
                ) { spec ->
                    if ((error != null) && posts.isEmpty()) {
                        item {
                            Text(
                                text = error!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 16.dp).transformedHeight(this, spec),
                            )
                        }
                    } else if (posts.isEmpty() && !isRefreshing) {
                        item {
                            Text(
                                text = stringResource(R.string.explore_empty),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.transformedHeight(this, spec),
                            )
                        }
                    } else {
                        items(posts.size) { index ->
                            val post = posts[index]
                            PostCard(
                                post = post,
                                spec = spec,
                                itemScope = this,
                            ) {
                                selectedPostStack.add(post)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PostCard(
    post: SnPost,
    spec: TransformationSpec,
    itemScope: TransformingLazyColumnItemScope,
    isDetail: Boolean = false,
    onClick: () -> Unit,
) {
    val author = post.publisher ?: post.author ?: post.account ?: post.user ?: post.creator ?: post.profile
    val avatarAttachment = author?.picture ?: author?.profile?.picture ?: author?.avatar?.let { 
        if (it.length > 20) SnAttachment(id = it, type = 0, url = it, previewUrl = it) else null 
    }
    
    val name = author?.nick ?: author?.nickname ?: author?.displayName ?: author?.name ?: author?.username ?: "Unknown User"
    val time = RelativeTime.format(post.publishedAt ?: post.createdAt)
    val replyCount = if ((post.repliesCount ?: 0) > 0) post.repliesCount else post.replyCount

    AppCard(
        onClick = onClick,
        enabled = (!isDetail) || ((replyCount ?: 0) > 0),
        modifier = Modifier
            .fillMaxWidth(if (isDetail) 1f else 0.94f)
            .padding(vertical = if (isDetail) 0.dp else 4.dp)
            .let { if (!isDetail) it.transformedHeight(itemScope, spec) else it },
        appName = {
            Text(
                text = name + (author?.status?.icon ?: ""),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.primary,
            )
        },
        appImage = {
            AsyncImage(
                model = ApiClient.resolveUrl(
                    author?.avatarUrl ?: author?.profile?.picture?.url ?: author?.picture?.url ?: author?.avatar,
                    avatarAttachment,
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentScale = ContentScale.Crop,
            )
        },
        time = { 
            Text(
                text = time,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ) 
        },
        title = {
            if (!post.title.isNullOrBlank()) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            } else {
                Spacer(Modifier.width(0.dp))
            }
        },
        shape = if (isDetail) RectangleShape else MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isDetail) MaterialTheme.colorScheme.surfaceContainerLow else MaterialTheme.colorScheme.surfaceContainer,
        ),
        transformation = if (!isDetail) with(itemScope) { SurfaceTransformation(spec) } else null,
    ) {
        if (!post.content.isNullOrBlank()) {
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodySmall,
                maxLines = if (isDetail) Int.MAX_VALUE else 5,
                overflow = if (isDetail) TextOverflow.Clip else TextOverflow.Ellipsis,
            )
        }

        // Attachments (Images)
        val images = (post.attachments ?: emptyList()) + (post.gallery ?: emptyList()) + (post.media ?: emptyList())
        val filteredImages = images.filter { (it.type == 0) || (it.mimeType?.startsWith("image/") == true) }
        
        if (isDetail) {
            filteredImages.forEach { attachment ->
                Spacer(Modifier.height(8.dp))
                AsyncImage(
                    model = ApiClient.resolveUrl(attachment.previewUrl ?: attachment.url, attachment),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentScale = ContentScale.FillWidth,
                )
            }
        } else {
            filteredImages.firstOrNull()?.let { attachment ->
                Spacer(Modifier.height(8.dp))
                AsyncImage(
                    model = ApiClient.resolveUrl(attachment.previewUrl ?: attachment.url, attachment),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentScale = ContentScale.Crop,
                )
            }
        }

        // Footer (Replies)
        if ((!isDetail) && ((replyCount ?: 0) > 0)) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = replyCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PostDetailScreen(
    post: SnPost,
    onBack: () -> Unit,
    onPostClick: (SnPost) -> Unit,
) {
    BackHandler(onBack = onBack)
    var comments by remember { mutableStateOf<List<SnPost>>(emptyList()) }
    var isLoading by remember { mutableStateOf(value = true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(post.id) {
        scope.launch {
            try {
                // If server-side filtering is unreliable, fetch more and filter locally
                val all = ApiClient.api.getTimeline(take = 100)
                comments = all.filter { 
                    (it.id != post.id) && (
                    (it.repliedPostId == post.id) || (it.parentId == post.id) || 
                    (it.replyToId == post.id) || (it.parent?.id == post.id) || 
                    (it.replyTo?.id == post.id) || (it.repliedPost?.id == post.id))
                }
            } catch (_: Exception) { }
            finally { isLoading = false }
        }
    }

    WearScreen { spec ->
        item {
            TextButton(onClick = onBack) {
                Text("← Back", style = MaterialTheme.typography.labelSmall)
            }
        }
        item {
            // isDetail=true disables transformation to keep it static at top
            PostCard(post = post, spec = spec, itemScope = this, isDetail = true) {
                // No-op for detail click
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Comments",
                style = MaterialTheme.typography.titleSmall, 
                modifier = Modifier.transformedHeight(this, spec),
            )
        }
        if (isLoading) {
            item { CircularProgressIndicator(modifier = Modifier.size(24.dp).transformedHeight(this, spec)) }
        } else if (comments.isEmpty()) {
            item {
                Text(
                    text = "No comments yet",
                    style = MaterialTheme.typography.bodySmall, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.transformedHeight(this, spec),
                )
            }
        } else {
            items(comments.size) { index ->
                val comment = comments[index]
                PostCard(post = comment, spec = spec, itemScope = this) {
                    onPostClick(comment)
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}
