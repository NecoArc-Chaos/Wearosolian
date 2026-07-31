package dev.solsynth.solian.ui.scaffold

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material3.MaterialTheme
import dev.solsynth.solian.theme.rememberIsAmbient
import dev.solsynth.solian.theme.rememberIsScreenRound

@Composable
fun WearScreen(
    modifier: Modifier = Modifier,
    content: ScalingLazyListScope.() -> Unit,
) {
    val listState = rememberScalingLazyListState()
    val focusRequester = remember { FocusRequester() }
    val rotaryBehavior = RotaryScrollableDefaults.behavior(scrollableState = listState)
    val isRound = rememberIsScreenRound()
    val isAmbient = rememberIsAmbient()

    val defaultTopPadding = if (isRound) 20.dp else 8.dp
    val defaultBottomPadding = if (isRound) 20.dp else 12.dp

    val backgroundColor = MaterialTheme.colorScheme.background

    ScalingLazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .rotaryScrollable(rotaryBehavior, focusRequester),
        state = listState,
        contentPadding = PaddingValues(
            top = defaultTopPadding,
            bottom = defaultBottomPadding,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        content()
    }
}
