package dev.solsynth.solian.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.OutlinedTextField
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material3.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import dev.solsynth.solian.data.TokenStore
import dev.solsynth.solian.data.api.ApiClient
import dev.solsynth.solian.data.model.ChallengeRequest
import dev.solsynth.solian.data.model.TokenExchangeRequest
import dev.solsynth.solian.theme.LocalScreenRound

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var serverUrl by remember { mutableStateOf(TokenStore.serverUrl) }
    var account by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(0) }
    var challengeId by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val listState = rememberScalingLazyListState()
    val focusRequester = remember { FocusRequester() }
    val rotaryBehavior = RotaryScrollableDefaults.behavior(scrollableState = listState)
    val isRound = LocalScreenRound.current

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .rotaryScrollable(rotaryBehavior, focusRequester),
        state = listState,
        topPadding = if (isRound) 36f else 8f,
        bottomPadding = if (isRound) 36f else 8f,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Title
        item {
            Text("Solian", style = MaterialTheme.typography.titleMedium)
        }
        item {
            Text("Solar Network",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        when (step) {
            0 -> {
                item { Spacer(Modifier.height(16.dp)) }

                // Server URL
                item {
                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = { serverUrl = it },
                        label = { Text("Server") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(0.9f),
                    )
                }

                // Account
                item { Spacer(Modifier.height(6.dp)) }
                item {
                    OutlinedTextField(
                        value = account,
                        onValueChange = { account = it },
                        label = { Text("Name or Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(0.9f),
                    )
                }

                // Error
                if (error != null) {
                    item {
                        Card(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth(0.85f),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            ),
                        ) {
                            Text(
                                error!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                            )
                        }
                    }
                }

                // Login button
                item { Spacer(Modifier.height(12.dp)) }
                item {
                    Button(
                        onClick = {
                            error = null
                            TokenStore.serverUrl = serverUrl
                            scope.launch {
                                try {
                                    val c = ApiClient.api.createChallenge(
                                        ChallengeRequest(account = account)
                                    )
                                    challengeId = c.id
                                    step = 1
                                    pollForApproval(c.id) { result ->
                                        when (result) {
                                            is PollResult.Approved -> {
                                                step = 2
                                                scope.launch {
                                                    delay(500)
                                                    onLoginSuccess()
                                                }
                                            }
                                            is PollResult.Error -> {
                                                error = result.message
                                                step = 0
                                            }
                                            is PollResult.Expired -> {
                                                error = "Expired. Try again."
                                                step = 0
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    error = e.message ?: "Connection failed"
                                }
                            }
                        },
                        enabled = account.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(0.7f),
                    ) { Text("Login") }
                }
            }

            1 -> {
                item {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
                item {
                    Text("Check your phone",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center)
                }
                item {
                    Text("Approve the login request",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center)
                }
            }

            2 -> {
                item {
                    Text("Approved!", style = MaterialTheme.typography.bodySmall)
                }
                item {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

sealed class PollResult {
    data object Approved : PollResult()
    data class Error(val message: String) : PollResult()
    data object Expired : PollResult()
}

private suspend fun pollForApproval(
    challengeId: String,
    onResult: (PollResult) -> Unit,
) {
    repeat(60) {
        delay(2000)
        try {
            val challenge = ApiClient.api.getChallenge(challengeId)
            if (challenge.approvedAt != null) {
                val tokenResp = ApiClient.api.exchangeToken(
                    TokenExchangeRequest(code = challengeId)
                )
                TokenStore.token = tokenResp.token
                onResult(PollResult.Approved)
                return
            }
        } catch (_: Exception) { }
    }
    onResult(PollResult.Expired)
}
