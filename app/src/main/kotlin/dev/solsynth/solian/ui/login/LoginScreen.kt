package dev.solsynth.solian.ui.login


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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.wear.compose.material3.*
import kotlinx.coroutines.launch
import dev.solsynth.solian.R
import dev.solsynth.solian.data.TokenStore
import dev.solsynth.solian.data.api.ApiClient
import dev.solsynth.solian.data.model.*
import dev.solsynth.solian.theme.OnSurfaceHigh
import dev.solsynth.solian.theme.OnSurfaceMedium
import dev.solsynth.solian.theme.SolianViolet
import dev.solsynth.solian.ui.scaffold.WearScreen

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var showQrLogin by remember { mutableStateOf(value = false) }
    val loginFailedMessage = stringResource(R.string.error_login_failed)

    key(showQrLogin) {
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
}

@Composable
private fun PasswordLoginScreen(
    onLoginSuccess: () -> Unit,
    onShowQrLogin: () -> Unit,
    loginFailedMessage: String,
) {

    var account by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val noPasswordFactorError = stringResource(R.string.error_no_password_factor)

    WearScreen {
        item {
            Text(
                text = stringResource(R.string.login_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
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
                value = account,
                onValueChange = { account = it },
                label = {
                    Text(
                        text = stringResource(R.string.login_account),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                singleLine = true,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(0.9f),
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = OnSurfaceHigh,
                    unfocusedTextColor = OnSurfaceMedium,
                    cursorColor = SolianViolet,
                ),
            )
        }

        item { Spacer(Modifier.height(6.dp)) }
        item {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = {
                    Text(
                        text = stringResource(R.string.login_password),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                singleLine = true,
                enabled = !isLoading,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(0.9f),
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = OnSurfaceHigh,
                    unfocusedTextColor = OnSurfaceMedium,
                    cursorColor = SolianViolet,
                ),
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
                    shape = MaterialTheme.shapes.medium,
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
                    try {
                        error = null
                        isLoading = true
                        scope.launch {
                            try {
                                val ch = ApiClient.api.createChallenge(
                                    ChallengeRequest(
                                        account = account,
                                        deviceId = TokenStore.deviceId,
                                    ),
                                )
                                val challengeId = ch.id ?: throw Exception("Invalid challenge ID")
                                val factors = ApiClient.api.getChallengeFactors(challengeId)
                                val pwFactor = factors.firstOrNull {
                                    it.name?.contains("password", true) == true
                                } ?: factors.firstOrNull { (it.type == 0) && (it.enabledAt != null) }
                                    ?: factors.firstOrNull { it.type == 0 }
                                    ?: throw Exception(noPasswordFactorError)

                                val factorId = pwFactor.id ?: throw Exception("Invalid factor ID")
                                ApiClient.api.performChallenge(
                                    challengeId,
                                    PerformChallengeRequest(factorId = factorId, password = password),
                                )

                                val tokenResp = ApiClient.api.exchangeToken(
                                    TokenExchangeRequest(code = challengeId),
                                )
                                TokenStore.token = tokenResp.token ?: throw Exception("Login failed: empty token")
                                TokenStore.refreshToken = tokenResp.refreshToken
                                tokenResp.expiresIn?.let {
                                    TokenStore.tokenExpiresAt = (System.currentTimeMillis() / 1000) + it
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
                    } catch (e: Exception) {
                        error = e.message ?: loginFailedMessage
                        isLoading = false
                    }
                },
                enabled = account.isNotBlank() && password.isNotBlank() && !isLoading,
                modifier = Modifier.fillMaxWidth(0.7f),
                colors = ButtonDefaults.buttonColors(
                    contentColor = OnSurfaceHigh,
                ),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.login_button),
                    )
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }

        // QR Login button
        item {
            Button(
                onClick = {
                    onShowQrLogin()
                },
                modifier = Modifier.fillMaxWidth(0.7f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
            ) {
                Text(
                    text = stringResource(R.string.login_scan_qr),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
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
