package dev.solsynth.solian.ui.scaffold

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
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

@Composable
fun WearScalingColumn(
    modifier: Modifier = Modifier,
    itemCount: Int,
    itemContent: @Composable (index: Int) -> Unit,
) {
    val listState = rememberTransformingLazyColumnState()
    val focusRequester = FocusRequester()
    val rotaryBehavior = RotaryScrollableDefaults.behavior(scrollableState = listState)

    TransformingLazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .rotaryScrollable(rotaryBehavior, focusRequester),
        state = listState,
    ) {
        items(itemCount) { index ->
            itemContent(index)
        }
    }
}
