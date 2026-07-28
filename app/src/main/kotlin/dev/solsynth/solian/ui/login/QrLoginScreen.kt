package dev.solsynth.solian.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material3.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import dev.solsynth.solian.data.TokenStore
import dev.solsynth.solian.data.api.ApiClient
import dev.solsynth.solian.data.model.QrGenerateRequest
import dev.solsynth.solian.data.model.QrStatusResponse
import dev.solsynth.solian.data.model.TokenExchangeRequest
import dev.solsynth.solian.theme.rememberIsScreenRound
import io.github.alexzhirkevich.qrose.rememberQrCodePainter

@Composable
fun QrLoginScreen(onLoginSuccess: () -> Unit, onBack: () -> Unit) {
    var qrData by remember { mutableStateOf<String?>(null) }
    var authChallengeId by remember { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf(0) } // 0=Pending, 1=Scanned, 2=Approved, 3=Declined
    var remainingSeconds by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val listState = rememberScalingLazyListState()
    val rotaryBehavior = RotaryScrollableDefaults.behavior(scrollableState = listState)
    val isRound = rememberIsScreenRound()

    // Generate QR challenge
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val resp = ApiClient.api.generateQrChallenge(QrGenerateRequest())
            qrData = resp.qrData
            authChallengeId = resp.authChallengeId
            remainingSeconds = resp.expiresInSeconds ?: 300
        } catch (e: Exception) {
            error = e.message ?: "Failed to generate QR"
        } finally {
            isLoading = false
        }
    }

    // Countdown timer
    LaunchedEffect(remainingSeconds) {
        while (remainingSeconds > 0 && status < 2) {
            delay(1000)
            remainingSeconds--
        }
    }

    // Poll for QR status every 2s
    LaunchedEffect(qrData, status) {
        if (qrData == null || status >= 2) return@LaunchedEffect
        while (status < 2 && remainingSeconds > 0) {
            delay(2000)
            try {
                val qrId = qrData!!.substringAfterLast("/")
                val qrStatus = ApiClient.api.getQrStatus(qrId)
                status = qrStatus.status
                if (status == 2 && authChallengeId != null) {
                    // Approved — exchange for token
                    val tokenResp = ApiClient.api.exchangeToken(
                        TokenExchangeRequest(code = authChallengeId!!)
                    )
                    TokenStore.token = tokenResp.token
                    TokenStore.refreshToken = tokenResp.refreshToken
                    tokenResp.expiresIn?.let {
                        TokenStore.tokenExpiresAt = System.currentTimeMillis() / 1000 + it
                    }
                    onLoginSuccess()
                }
            } catch (_: Exception) { }
        }
    }

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .rotaryScrollable(rotaryBehavior, remember { FocusRequester() }),
        state = listState,
        contentPadding = PaddingValues(
            top = if (isRound) 36.dp else 8.dp,
            bottom = if (isRound) 36.dp else 8.dp,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Text("Scan to Login", style = MaterialTheme.typography.titleSmall)
        }

        item { Spacer(Modifier.height(8.dp)) }

        if (isLoading) {
            item {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            }
        } else if (error != null) {
            item {
                Card(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(0.85f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                    ),
                ) {
                    Text(
                        error!!, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(8.dp).fillMaxWidth(),
                    )
                }
            }
        } else if (qrData != null) {
            item {
                val painter = rememberQrCodePainter(qrData!!)
                Card(
                    onClick = {},
                    modifier = Modifier.size(120.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White,
                    ),
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        androidx.compose.foundation.Image(
                            painter = painter,
                            contentDescription = "QR Code",
                            modifier = Modifier.size(100.dp),
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }

            // Status chip
            item {
                val (label, color) = when (status) {
                    1 -> "Scanned" to MaterialTheme.colorScheme.tertiary
                    2 -> "Approved" to MaterialTheme.colorScheme.primary
                    3 -> "Declined" to MaterialTheme.colorScheme.error
                    else -> "Waiting..." to MaterialTheme.colorScheme.onSurfaceVariant
                }
                Text(label, style = MaterialTheme.typography.labelSmall, color = color)
            }

            // Countdown
            item {
                if (remainingSeconds > 0) {
                    Text("${remainingSeconds}s", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text("Expired", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error)
                }
            }

            item { Spacer(Modifier.height(8.dp)) }

            // Refresh button
            item {
                Button(
                    onClick = {
                        error = null
                        scope.launch {
                            isLoading = true
                            try {
                                val resp = ApiClient.api.generateQrChallenge(QrGenerateRequest())
                                qrData = resp.qrData
                                authChallengeId = resp.authChallengeId
                                remainingSeconds = resp.expiresInSeconds ?: 300
                                status = 0
                            } catch (e: Exception) {
                                error = e.message ?: "Failed"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading && (remainingSeconds <= 0 || status == 3),
                    modifier = Modifier.fillMaxWidth(0.7f),
                ) { Text("Refresh") }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }

        // Back button
        item {
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(0.5f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
            ) { Text("Back") }
        }
    }
}
