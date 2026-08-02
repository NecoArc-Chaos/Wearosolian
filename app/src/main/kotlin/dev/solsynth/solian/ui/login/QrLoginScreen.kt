package dev.solsynth.solian.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material3.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import dev.solsynth.solian.R
import dev.solsynth.solian.data.TokenStore
import dev.solsynth.solian.data.api.ApiClient
import dev.solsynth.solian.data.model.QrGenerateRequest
import dev.solsynth.solian.data.model.TokenExchangeRequest
import dev.solsynth.solian.theme.rememberIsScreenRound
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import dev.solsynth.solian.ui.scaffold.WearScreen

@Composable
fun QrLoginScreen(onLoginSuccess: () -> Unit, onBack: () -> Unit) {
    var qrData by remember { mutableStateOf<String?>(null) }
    var qrChallengeId by remember { mutableStateOf<String?>(null) }
    var authChallengeId by remember { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf("Pending") }
    var remainingSeconds by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun generateQr() {
        error = null
        isLoading = true
        scope.launch {
            try {
                val resp = ApiClient.api.generateQrChallenge(QrGenerateRequest())
                qrData = resp.qrData
                qrChallengeId = resp.qrChallengeId
                authChallengeId = resp.authChallengeId
                remainingSeconds = resp.expiresInSeconds ?: 300
                status = "Pending"
            } catch (e: Exception) {
                error = e.message ?: context.getString(R.string.qr_error_generate_failed)
            } finally {
                isLoading = false
            }
        }
    }

    // Generate on first load
    LaunchedEffect(Unit) { generateQr() }

    // Countdown timer
    LaunchedEffect(remainingSeconds, status) {
        while (remainingSeconds > 0 && status !in listOf("Approved", "Declined")) {
            delay(1000)
            remainingSeconds--
        }
    }

    // Poll for QR status every 2s using reactive flow
    LaunchedEffect(qrChallengeId) {
        val id = qrChallengeId ?: return@LaunchedEffect
        snapshotFlow { status to remainingSeconds }
            .filter { (s, r) -> s !in listOf("Approved", "Declined") && r > 0 }
            .flatMapLatest {
                flow {
                    while (true) {
                        delay(2000)
                        emit(Unit)
                    }
                }
            }
            .collect {
                try {
                    val qrStatus = ApiClient.api.getQrStatus(id)
                    status = qrStatus.status
                    if (status == "Approved" && authChallengeId != null) {
                        val challengeId = authChallengeId!!
                        val tokenResp = ApiClient.api.exchangeToken(
                            TokenExchangeRequest(code = challengeId)
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

    WearScreen {
        item { Text(stringResource(R.string.qr_scan_title), style = MaterialTheme.typography.titleSmall) }
        item { Spacer(Modifier.height(8.dp)) }

        when {
            isLoading -> item {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            }
            error != null -> item {
                Card(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(0.85f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                    ),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(error!!, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(8.dp).fillMaxWidth())
                }
            }
            qrData != null -> {
                item {
                    val painter = rememberQrCodePainter(qrData!!)
                    Card(onClick = {}, modifier = Modifier.size(120.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Image(painter, "QR Code", Modifier.size(100.dp))
                        }
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
                // Status
                item {
                    val (label, color) = when (status) {
                        "Scanned" -> stringResource(R.string.qr_status_scanned) to MaterialTheme.colorScheme.tertiary
                        "Approved" -> stringResource(R.string.qr_status_approved) to MaterialTheme.colorScheme.primary
                        "Declined" -> stringResource(R.string.qr_status_declined) to MaterialTheme.colorScheme.error
                        else -> stringResource(R.string.qr_status_waiting) to MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Text(label, style = MaterialTheme.typography.labelSmall, color = color)
                }
                // Countdown
                item {
                    if (remainingSeconds > 0) {
                        Text(stringResource(R.string.qr_remaining_seconds, remainingSeconds), style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text(stringResource(R.string.qr_expired), style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error)
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
                // Refresh button (only when expired or declined)
                item {
                    Button(
                        onClick = { generateQr() },
                        enabled = !isLoading && (remainingSeconds <= 0 || status == "Declined"),
                        modifier = Modifier.fillMaxWidth(0.7f),
                    ) { Text(stringResource(R.string.qr_refresh)) }
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
        item {
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(0.5f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
            ) { Text(stringResource(R.string.qr_back)) }
        }
    }
}
