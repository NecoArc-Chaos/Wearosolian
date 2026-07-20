package dev.solsynth.solian.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.FilterChip
import androidx.wear.compose.material3.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import dev.solsynth.solian.data.TokenStore
import dev.solsynth.solian.data.api.ApiClient
import dev.solsynth.solian.data.model.ChallengeRequest
import dev.solsynth.solian.data.model.TokenExchangeRequest

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var serverUrl by remember { mutableStateOf(TokenStore.serverUrl) }
    var account by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(0) }
    var challengeId by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Solian", style = MaterialTheme.typography.titleMedium)

        when (step) {
            0 -> {
                Spacer(Modifier.height(12.dp))

                TextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it },
                    label = { Text("Server") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))

                TextField(
                    value = account,
                    onValueChange = { account = it },
                    label = { Text("Name or Email") },
                    modifier = Modifier.fillMaxWidth(),
                )

                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(12.dp))

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
                                error = e.message ?: "Failed"
                            }
                        }
                    },
                    enabled = account.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(0.8f),
                ) { Text("Login") }
            }

            1 -> {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                Spacer(Modifier.height(8.dp))
                Text("Check your phone",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center)
            }

            2 -> {
                Text("Approved!", style = MaterialTheme.typography.bodySmall)
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
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
