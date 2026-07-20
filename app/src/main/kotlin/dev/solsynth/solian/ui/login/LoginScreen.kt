package dev.solsynth.solian.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
    var step by remember { mutableStateOf(0) } // 0=input, 1=pending, 2=approved
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
                // Step 0: Enter email/username
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it },
                    label = { Text("Server") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = account,
                    onValueChange = { account = it },
                    label = { Text("Username or Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        scope.launch {
                            error = null
                            TokenStore.serverUrl = serverUrl
                            try {
                                val challenge = ApiClient.api.createChallenge(
                                    ChallengeRequest(account = account)
                                )
                                challengeId = challenge.id
                                step = 1
                                // Start polling
                                pollForApproval(challenge.id) { result ->
                                    when (result) {
                                        is PollResult.Approved -> {
                                            step = 2
                                            delay(500)
                                            onLoginSuccess()
                                        }
                                        is PollResult.Error -> {
                                            error = result.message
                                            step = 0
                                        }
                                        is PollResult.Expired -> {
                                            error = "Challenge expired. Try again."
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
                    modifier = Modifier.fillMaxWidth(0.8f),
                ) { Text("Login") }
            }

            1 -> {
                // Step 1: Waiting for phone approval
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                Spacer(Modifier.height(8.dp))
                Text("Check your phone",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center)
                Text("Approve the login request",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center)
            }

            2 -> {
                // Step 2: Approved, logging in
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
    repeat(60) { // ~1 minute timeout
        delay(2000)
        try {
            val challenge = ApiClient.api.getChallenge(challengeId)
            when {
                challenge.status == "approved" || challenge.stepTotal == 0 -> {
                    // Exchange challenge for token
                    val tokenResp = ApiClient.api.exchangeToken(
                        // The "code" is the challenge ID itself for token exchange
                        TokenExchangeRequest(code = challengeId)
                    )
                    TokenStore.token = tokenResp.token
                    onResult(PollResult.Approved)
                    return
                }
                challenge.status == "declined" -> {
                    onResult(PollResult.Error("Login declined"))
                    return
                }
            }
        } catch (e: Exception) {
            // Continue polling on transient errors
        }
    }
    onResult(PollResult.Expired)
}
