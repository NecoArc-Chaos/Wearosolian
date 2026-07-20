package dev.solsynth.solian.ui.chat

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
fun ChatScreen() {
    val listState = rememberScalingLazyListState()
    val focusRequester = remember { FocusRequester() }
    val rotaryBehavior = RotaryScrollableDefaults.behavior(scrollableState = listState)

    // Placeholder chat rooms — will be replaced with real API data
    val rooms = remember {
        listOf(
            Triple("General", "Alice: See you there!", "2m ago"),
            Triple("Project X", "Bob: PR merged ✅", "15m ago"),
            Triple("Random", "Carol: lol nice one 😂", "1h ago"),
        )
    }

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .rotaryScrollable(rotaryBehavior, focusRequester),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item { Spacer(Modifier.height(8.dp)) }

        item {
            Text("Messages", style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.fillMaxWidth(0.9f))
        }

        items(rooms.size) { index ->
            val (name, preview, time) = rooms[index]
            Card(
                onClick = {},
                modifier = Modifier.fillMaxWidth(0.9f).padding(vertical = 4.dp),
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(name, style = MaterialTheme.typography.labelSmall)
                        Text(time, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(preview, style = MaterialTheme.typography.bodySmall,
                        maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}
