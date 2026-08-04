package dev.solsynth.solian.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.*
import androidx.wear.compose.material3.lazy.transformedHeight
import coil.compose.AsyncImage
import dev.solsynth.solian.R
import dev.solsynth.solian.data.api.ApiClient
import dev.solsynth.solian.data.model.AccountStatusRequest
import dev.solsynth.solian.data.model.AuthConstants
import dev.solsynth.solian.data.model.SnAttachment
import dev.solsynth.solian.ui.scaffold.WearScreen
import kotlinx.coroutines.launch

@Composable
fun AccountScreen(onLogout: () -> Unit) {
    var userName by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    var presence by remember { mutableStateOf(true) }
    var isBusyStatus by remember { mutableStateOf(false) }
    val statusSymbol = remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val me = ApiClient.api.getMe()
                userName = me.nick ?: me.nickname ?: me.displayName ?: me.name ?: me.username ?: me.id?.take(8) ?: ""
                val avatarAttachment = me.picture ?: me.profile?.picture ?: me.avatar?.let { 
                    if (it.length > 20) SnAttachment(id = it, type = 0, url = it, previewUrl = it) else null 
                }
                val url = me.avatarUrl ?: me.profile?.picture?.url ?: me.picture?.url ?: me.avatar
                avatarUrl = ApiClient.resolveUrl(url, avatarAttachment)
            } catch (_: Exception) { }

            try {
                val status = ApiClient.api.getMyStatus()
                presence = status.type != AuthConstants.STATUS_TYPE_INVISIBLE
                isBusyStatus = status.type == AuthConstants.STATUS_TYPE_BUSY
                if (!status.icon.isNullOrBlank()) {
                    statusSymbol.value = status.icon
                }
            } catch (_: Exception) { }
        }
    }

    WearScreen { spec ->
        item {
            AsyncImage(
                model = avatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .transformedHeight(this, spec),
                contentScale = ContentScale.Crop
            )
        }

        item {
            Text(
                text = (userName.ifBlank { stringResource(R.string.account_title) }) + statusSymbol.value,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.transformedHeight(this, spec)
            )
        }

        item { Spacer(Modifier.height(12.dp)) }

        item {
            ListHeader(
                modifier = Modifier.transformedHeight(this, spec)
            ) {
                Text(stringResource(R.string.account_quick_status))
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(0.9f).transformedHeight(this, spec),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = {
                        val nextPresence = !presence
                        scope.launch {
                            isLoading = true
                            try {
                                val updated = ApiClient.api.updateStatus(
                                    AccountStatusRequest(
                                        type = if (nextPresence) AuthConstants.STATUS_TYPE_NORMAL else AuthConstants.STATUS_TYPE_INVISIBLE,
                                    )
                                )
                                presence = updated.type != AuthConstants.STATUS_TYPE_INVISIBLE
                                if (!updated.icon.isNullOrBlank()) statusSymbol.value = updated.icon
                            } catch (_: Exception) { }
                            isLoading = false
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = if (presence) ButtonDefaults.buttonColors() else ButtonDefaults.filledTonalButtonColors(),
                    enabled = !isLoading
                ) {
                    Text(
                        text = if (presence) stringResource(R.string.account_status_online) else stringResource(R.string.account_status_offline),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                
                Button(
                    onClick = {
                        val nextBusy = !isBusyStatus
                        scope.launch {
                            isLoading = true
                            try {
                                val updated = ApiClient.api.updateStatus(
                                    AccountStatusRequest(
                                        type = if (nextBusy) {
                                            AuthConstants.STATUS_TYPE_BUSY
                                        } else {
                                            if (presence) AuthConstants.STATUS_TYPE_NORMAL else AuthConstants.STATUS_TYPE_INVISIBLE
                                        },
                                        label = if (nextBusy) "Busy" else null,
                                    )
                                )
                                isBusyStatus = updated.type == AuthConstants.STATUS_TYPE_BUSY
                                if (!updated.icon.isNullOrBlank()) statusSymbol.value = updated.icon
                            } catch (_: Exception) { }
                            isLoading = false
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = if (isBusyStatus) {
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    } else {
                        ButtonDefaults.filledTonalButtonColors()
                    },
                    enabled = !isLoading
                ) {
                    Text(
                        text = stringResource(R.string.account_status_busy),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }

        item {
            FilledTonalButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(0.8f).transformedHeight(this, spec),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
            ) {
                Text(text = stringResource(R.string.account_logout))
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}
