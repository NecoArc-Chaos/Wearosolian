package dev.solsynth.solian.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.OutlinedTextField
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material3.*
import kotlinx.coroutines.launch
import dev.solsynth.solian.data.TokenStore
import dev.solsynth.solian.data.api.ApiClient
import dev.solsynth.solian.data.model.*
import dev.solsynth.solian.theme.LocalScreenRound

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var serverUrl by remember { mutableStateOf(TokenStore.serverUrl) }
    var account by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
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
        item {
            Text("Solian", style = MaterialTheme.typography.titleMedium)
        }
        item {
            Text("Solar Network",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item { Spacer(Modifier.height(16.dp)) }

        // Server URL
        item {
            OutlinedTextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                label = { Text("Server") },
                singleLine = true,
                enabled = !isLoading,
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
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(0.9f),
            )
        }

        // Password
        item { Spacer(Modifier.height(6.dp)) }
        item {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                enabled = !isLoading,
                visualTransformation = PasswordVisualTransformation(),
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
                    Text(error!!, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(8.dp).fillMaxWidth())
                }
            }
        }

        // Login button
        item { Spacer(Modifier.height(12.dp)) }
        item {
            Button(
                onClick = {
                    error = null
                    isLoading = true
                    TokenStore.serverUrl = serverUrl
                    scope.launch {
                        try {
                            // 1. Create challenge
                            val ch = ApiClient.api.createChallenge(
                                ChallengeRequest(account = account)
                            )
                            // 2. Get factors, find password factor (type=0)
                            val factors = ApiClient.api.getChallengeFactors(ch.id)
                            val pwFactor = factors.firstOrNull { it.type == 0 }
                                ?: throw Exception("No password factor configured")

                            // 3. Verify password
                            ApiClient.api.performChallenge(
                                ch.id,
                                PerformChallengeRequest(factorId = pwFactor.id, password = password)
                            )
                            // 4. Exchange for token
                            val tokenResp = ApiClient.api.exchangeToken(
                                TokenExchangeRequest(code = ch.id)
                            )
                            TokenStore.token = tokenResp.token
                            onLoginSuccess()
                        } catch (e: Exception) {
                            error = e.message ?: "Login failed"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = account.isNotBlank() && password.isNotBlank() && !isLoading,
                modifier = Modifier.fillMaxWidth(0.7f),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Login")
                }
            }
        }
    }
}
