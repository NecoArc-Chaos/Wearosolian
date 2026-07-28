package dev.solsynth.solian.ui.scaffold

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.TimeText

@Composable
fun WearAppScaffold(content: @Composable () -> Unit) {
    AppScaffold(
        timeText = { TimeText() },
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
    ) {
        content()
    }
}
