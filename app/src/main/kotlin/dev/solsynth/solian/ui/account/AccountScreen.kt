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
import androidx.compose.ui.platform.LocalContext
import dev.solsynth.solian.R
import dev.solsynth.solian.data.TokenStore
import dev.solsynth.solian.data.api.ApiClient
import dev.solsynth.solian.data.model.AccountStatusRequest
import dev.solsynth.solian.data.model.AuthConstants
import dev.solsynth.solian.theme.ThemeStateManager
import dev.solsynth.solian.ui.scaffold.WearScreen

@Composable
fun AccountScreen(onLogout: () -> Unit, onShowLogin: () -> Unit) {
    val isLoggedIn = TokenStore.isLoggedIn
    val statusOnline = stringResource(R.string.status_online)
    val statusBusy = stringResource(R.string.status_busy)
    var statusText by remember { mutableStateOf(statusOnline) }
    var presence by remember { mutableStateOf(true) }
    var userName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    if (isLoggedIn) {
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
                    AuthConstants.STATUS_TYPE_INVISIBLE -> AuthConstants.STATUS_TYPE_INVISIBLE
                    else -> statusOnline
                }
                presence = status.isOnline ?: true
            } catch (_: Exception) {
                // Keep defaults if load fails
            }
        }
    }

    var dynamicColorEnabled by remember { mutableStateOf(ThemeStore.isDynamicColorEnabled) }

    WearScreen {
        if (isLoggedIn) {
            item {
                Card(
                    onClick = {},
                    modifier = Modifier.size(48.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.account_avatar), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            item { Text(stringResource(R.string.account_title), style = MaterialTheme.typography.titleSmall) }

            item { Spacer(Modifier.height(12.dp)) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.account_dynamic_color),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(
                        checked = dynamicColorEnabled,
                        onCheckedChange = { enabled ->
                            dynamicColorEnabled = enabled
                            ThemeStore.isDynamicColorEnabled = enabled
                            scope.launch {
                                ThemeStateManager.refreshDynamicColor(context)
                            }
                        },
                    )
                }
            }

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
                                            type = if (newPresence) AuthConstants.STATUS_TYPE_DEFAULT else AuthConstants.STATUS_TYPE_INVISIBLE,
                                            attitude = AuthConstants.STATUS_ATTITUDE_NEUTRAL,
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
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Text(
                            text = if (presence) stringResource(R.string.account_status_online) else stringResource(R.string.account_status_offline),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        )
                    }
                    Card(
                        onClick = {
                            val newStatus = if (statusText == statusBusy) statusOnline else statusBusy
                            scope.launch {
                                isLoading = true
                                try {
                                    ApiClient.api.updateStatus(
                                        AccountStatusRequest(
                                            type = AuthConstants.STATUS_TYPE_DEFAULT,
                                            attitude = if (newStatus == statusBusy) AuthConstants.STATUS_ATTITUDE_BUSY else AuthConstants.STATUS_ATTITUDE_NEUTRAL,
                                            label = if (newStatus == statusBusy) AuthConstants.STATUS_ATTITUDE_BUSY else null,
                                        )
                                    )
                                    statusText = newStatus
                                } catch (_: Exception) { }
                                isLoading = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .then(if (statusText == statusBusy) Modifier.background(MaterialTheme.colorScheme.primaryContainer) else Modifier),
                        colors = CardDefaults.cardColors(
                            containerColor = if (statusText == statusBusy) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                        ),
                        shape = MaterialTheme.shapes.small,
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
                ) {
                    Text(
                        text = stringResource(R.string.account_logout),
                        color = MaterialTheme.colorScheme.onError,
                    )
                }
            }

            item { Spacer(Modifier.height(12.dp)) }

            item {
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(0.6f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.account_switch_account),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        } else {
            item {
                Card(
                    onClick = {},
                    modifier = Modifier.size(48.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("?", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            item { Text(stringResource(R.string.account_title), style = MaterialTheme.typography.titleSmall) }

            item { Spacer(Modifier.height(12.dp)) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.account_dynamic_color),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(
                        checked = dynamicColorEnabled,
                        onCheckedChange = { enabled ->
                            dynamicColorEnabled = enabled
                            ThemeStore.isDynamicColorEnabled = enabled
                            scope.launch {
                                ThemeStateManager.refreshDynamicColor(context)
                            }
                        },
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            item {
                Button(
                    onClick = onShowLogin,
                    modifier = Modifier.fillMaxWidth(0.7f),
                ) {
                    Text(stringResource(R.string.login_button))
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}
