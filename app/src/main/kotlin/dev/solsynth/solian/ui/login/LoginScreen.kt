package dev.solsynth.solian.ui.login

import android.provider.Settings.Secure
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.OutlinedTextField
import androidx.wear.compose.material3.*
import kotlinx.coroutines.launch
import dev.solsynth.solian.R
import dev.solsynth.solian.data.TokenStore
import dev.solsynth.solian.data.api.ApiClient
import dev.solsynth.solian.data.model.*
import dev.solsynth.solian.ui.scaffold.WearScreen

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var showQrLogin by remember { mutableStateOf(false) }
    val loginFailedMessage = stringResource(R.string.error_login_failed)

    if (showQrLogin) {
        QrLoginScreen(
            onLoginSuccess = onLoginSuccess,
            onBack = { showQrLogin = false },
        )
    } else {
        PasswordLoginScreen(
            onLoginSuccess = onLoginSuccess,
            onShowQrLogin = { showQrLogin = true },
            loginFailedMessage = loginFailedMessage,
        )
    }
}

@Composable
private fun PasswordLoginScreen(
    onLoginSuccess: () -> Unit,
    onShowQrLogin: () -> Unit,
    loginFailedMessage: String,
) {
    val context = LocalContext.current
    var serverUrl by remember { mutableStateOf(TokenStore.serverUrl) }
    var account by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val deviceId = remember {
        Secure.getString(context.contentResolver, Secure.ANDROID_ID) ?: "wearos-unknown"
    }

    WearScreen {
        item { Text(stringResource(R.string.login_title), style = MaterialTheme.typography.titleMedium) }
        item {
            Text(
                text = stringResource(R.string.login_subtitle),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        item { Spacer(Modifier.height(16.dp)) }

        item {
            OutlinedTextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                label = { Text(stringResource(R.string.login_server)) },
                singleLine = true,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(0.9f),
            )
        }

        item { Spacer(Modifier.height(6.dp)) }
        item {
            OutlinedTextField(
                value = account,
                onValueChange = { account = it },
                label = { Text(stringResource(R.string.login_account)) },
                singleLine = true,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(0.9f),
            )
        }

        item { Spacer(Modifier.height(6.dp)) }
        item {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.login_password)) },
                singleLine = true,
                enabled = !isLoading,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(0.9f),
            )
        }

        if (error != null) {
            item {
                Card(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(0.85f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                    ),
                ) {
                    Text(
                        text = error!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                    )
                }
            }
        }

        item { Spacer(Modifier.height(12.dp)) }

        // Login button
        item {
            Button(
                onClick = {
                    error = null
                    isLoading = true
                    TokenStore.serverUrl = serverUrl
                    scope.launch {
                        try {
                            val ch = ApiClient.api.createChallenge(
                                ChallengeRequest(
                                    account = account,
                                    deviceId = deviceId,
                                ),
                            )
                            val factors = ApiClient.api.getChallengeFactors(ch.id)
                            val pwFactor = factors.firstOrNull {
                                it.name?.contains("password", true) == true
                            } ?: factors.firstOrNull { (it.type == 0) && (it.enabledAt != null) }
                                ?: factors.firstOrNull { it.type == 0 }
                                ?: throw Exception(getString(R.string.error_no_password_factor))

                            val result = ApiClient.api.performChallenge(
                                ch.id,
                                PerformChallengeRequest(factorId = pwFactor.id, password = password),
                            )

                            val tokenResp = ApiClient.api.exchangeToken(
                                TokenExchangeRequest(code = ch.id),
                            )
                            TokenStore.token = tokenResp.token
                            TokenStore.refreshToken = tokenResp.refreshToken
                            tokenResp.expiresIn?.let {
                                TokenStore.tokenExpiresAt = System.currentTimeMillis() / 1000 + it
                            }
                            onLoginSuccess()
                        } catch (e: retrofit2.HttpException) {
                            val apiError = parseApiError(e)
                            error = apiError ?: "HTTP ${e.code()}: ${e.message()}"
                        } catch (e: Exception) {
                            error = e.message ?: loginFailedMessage
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
                    Text(stringResource(R.string.login_button))
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }

        // QR Login button
        item {
            Button(
                onClick = {
                    TokenStore.serverUrl = serverUrl
                    ApiClient.recreate()
                    onShowQrLogin()
                },
                modifier = Modifier.fillMaxWidth(0.7f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
            ) { Text("Scan QR") }
        }
    }
}

private fun parseApiError(e: retrofit2.HttpException): String? {
    return try {
        val body = e.response()?.errorBody()?.string()
        if (body.isNullOrBlank()) return null
        val regex = """"message"\s*:\s*"([^"]+)"""".toRegex()
        regex.find(body)?.groupValues?.get(1)
    } catch (_: Exception) {
        null
    }
}
