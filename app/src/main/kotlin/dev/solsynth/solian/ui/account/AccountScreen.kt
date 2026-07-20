package dev.solsynth.solian.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material3.*

@Composable
fun AccountScreen(onLogout: () -> Unit) {
    val listState = rememberScalingLazyListState()
    val focusRequester = remember { FocusRequester() }
    val rotaryBehavior = RotaryScrollableDefaults.behavior(scrollableState = listState)
    var statusText by remember { mutableStateOf("Online") }
    var presence by remember { mutableStateOf(true) }

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .rotaryScrollable(rotaryBehavior, focusRequester),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item { Spacer(Modifier.height(16.dp)) }

        item {
            Card(
                onClick = {},
                modifier = Modifier.size(48.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("👤", style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        item { Text("User", style = MaterialTheme.typography.titleSmall) }

        item { Spacer(Modifier.height(12.dp)) }

        item {
            Text("Quick Status", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(0.9f))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                CompactChip(
                    onClick = { presence = !presence },
                    label = { Text(if (presence) "🟢 On" else "⚫ Off") },
                )
                CompactChip(
                    onClick = { statusText = if (statusText == "Busy") "Online" else "Busy" },
                    label = { Text("🔴 $statusText") },
                )
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
            ) { Text("Logout") }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}
