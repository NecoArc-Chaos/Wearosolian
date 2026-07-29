package dev.solsynth.solian.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.*
import kotlinx.coroutines.launch
import dev.solsynth.solian.R
import dev.solsynth.solian.data.api.ApiClient
import dev.solsynth.solian.data.model.AccountStatusRequest
import dev.solsynth.solian.ui.scaffold.WearScreen

@Composable
fun AccountScreen(onLogout: () -> Unit) {
    var statusText by remember { mutableStateOf("Online") }
    var presence by remember { mutableStateOf(true) }
    var userName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val me = ApiClient.api.getMe()
                userName = me.nick ?: me.name ?: ""
            } catch (_: Exception) {
                // Keep default if load fails
            }
        }
        try {
            val status = ApiClient.api.getMyStatus()
            statusText = when (status.type) {
                "Invisible" -> "Invisible"
                else -> "Online"
            }
            presence = status.isOnline ?: true
        } catch (_: Exception) {
            // Keep defaults if load fails
        }
    }

    WearScreen {
        item {
            Card(
                onClick = {},
                modifier = Modifier.size(48.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.account_avatar), style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        item { Text(stringResource(R.string.account_title), style = MaterialTheme.typography.titleSmall) }

        item { Spacer(Modifier.height(12.dp)) }

        item {
            Text(stringResource(R.string.account_quick_status), style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(0.9f))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Card(
                    onClick = {
                        val newPresence = !presence
                        scope.launch {
                            isLoading = true
                            try {
                                ApiClient.api.updateStatus(
                                    AccountStatusRequest(
                                        type = if (newPresence) "Default" else "Invisible",
                                        attitude = "Neutral",
                                    )
                                )
                                presence = newPresence
                            } catch (_: Exception) { }
                            isLoading = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .then(if (presence) Modifier.background(MaterialTheme.colorScheme.primaryContainer) else Modifier),
                    colors = CardDefaults.cardColors(
                        containerColor = if (presence) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
                ) {
                    Text(
                        text = if (presence) stringResource(R.string.account_status_online) else stringResource(R.string.account_status_offline),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
                Card(
                    onClick = {
                        val newStatus = if (statusText == "Busy") "Online" else "Busy"
                        scope.launch {
                            isLoading = true
                            try {
                                ApiClient.api.updateStatus(
                                    AccountStatusRequest(
                                        type = "Default",
                                        attitude = if (newStatus == "Busy") "Busy" else "Neutral",
                                        label = if (newStatus == "Busy") "Busy" else null,
                                    )
                                )
                                statusText = newStatus
                            } catch (_: Exception) { }
                            isLoading = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .then(if (statusText == "Busy") Modifier.background(MaterialTheme.colorScheme.primaryContainer) else Modifier),
                    colors = CardDefaults.cardColors(
                        containerColor = if (statusText == "Busy") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.account_status_busy),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }

        item {
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(0.6f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
            ) { Text(stringResource(R.string.account_logout)) }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}
