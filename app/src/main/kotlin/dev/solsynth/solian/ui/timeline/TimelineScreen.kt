package dev.solsynth.solian.ui.timeline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material3.Text

@Composable
fun TimelineScreen(
    onNavigateToChat: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Timeline")
        Text("Coming soon", color = androidx.wear.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
