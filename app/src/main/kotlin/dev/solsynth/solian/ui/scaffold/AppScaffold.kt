package dev.solsynth.solian.ui.scaffold

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.lazy.itemsIndexed
import androidx.wear.compose.material3.TimeText

@Composable
fun WearAppScaffold(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        TimeText()
        content()
    }
}

@Composable
fun WearScalingColumn(
    modifier: Modifier = Modifier,
    itemCount: Int,
    itemContent: @Composable (index: Int) -> Unit,
) {
    val listState = rememberScalingLazyListState()

    ScalingLazyColumn(
        modifier = modifier
            .fillMaxSize()
            .onRotaryScrollEvent { event ->
                val delta = event.verticalScrollPixels
                listState.centerItemIndex = (listState.centerItemIndex + (delta / 100).toInt())
                    .coerceIn(0, (itemCount - 1).coerceAtLeast(0))
                true
            },
        state = listState,
        autoCentering = true,
    ) {
        itemsIndexed(count = itemCount) { index, _ ->
            itemContent(index)
        }
    }
}
