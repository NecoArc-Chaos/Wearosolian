package dev.solsynth.solian.ui.scaffold

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.lazy.itemsIndexed
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.TimeText
import dev.solsynth.solian.theme.LocalScreenRound

/**
 * Wear OS app shell with TimeText and ScalingLazyColumn.
 */
@Composable
fun WearAppScaffold(
    content: @Composable () -> Unit
) {
    AppScaffold(
        timeText = { TimeText() }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

/**
 * A [ScalingLazyColumn] with crown (rotary) scroll support.
 * On round watches, extra padding is added so edge items can scroll to center.
 */
@Composable
fun WearScalingColumn(
    modifier: Modifier = Modifier,
    itemCount: Int,
    itemContent: @Composable (index: Int) -> Unit,
) {
    val listState = rememberScalingLazyListState()
    val isRound = LocalScreenRound.current

    ScalingLazyColumn(
        modifier = modifier
            .fillMaxSize()
            .onRotaryScrollEvent { event ->
                listState.animateScrollBy(event.verticalScrollPixels)
                true
            },
        state = listState,
        topPadding = if (isRound) 40f else 8f,
        bottomPadding = if (isRound) 40f else 8f,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        itemsIndexed(count = itemCount) { index ->
            itemContent(index)
        }
    }
}
