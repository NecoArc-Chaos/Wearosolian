package dev.solsynth.solian.ui.scaffold

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnScope
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import dev.solsynth.solian.theme.rememberIsScreenRound

@Composable
fun WearScreen(
    modifier: Modifier = Modifier,
    state: TransformingLazyColumnState = rememberTransformingLazyColumnState(),
    edgeButton: (@Composable BoxScope.() -> Unit)? = null,
    pullToRefreshState: PullToRefreshState? = null,
    isRefreshing: Boolean = false,
    onRefresh: (() -> Unit)? = null,
    content: TransformingLazyColumnScope.(TransformationSpec) -> Unit,
) {
    val isRound = rememberIsScreenRound()
    val transformationSpec = rememberTransformationSpec()

    if (edgeButton != null) {
        ScreenScaffold(
            scrollState = state,
            edgeButton = edgeButton,
            modifier = modifier.fillMaxSize(),
            scrollIndicator = {
                ScrollIndicator(state = state)
            }
        ) { padding ->
            WearScreenTLC(state, padding, true, isRound, transformationSpec, pullToRefreshState, isRefreshing, onRefresh, content)
        }
    } else {
        ScreenScaffold(
            scrollState = state,
            modifier = modifier.fillMaxSize(),
            scrollIndicator = {
                ScrollIndicator(state = state)
            }
        ) { padding ->
            WearScreenTLC(state, padding, false, isRound, transformationSpec, pullToRefreshState, isRefreshing, onRefresh, content)
        }
    }
}

@Composable
private fun WearScreenTLC(
    state: TransformingLazyColumnState,
    padding: PaddingValues,
    hasEdgeButton: Boolean,
    isRound: Boolean,
    transformationSpec: TransformationSpec,
    pullToRefreshState: PullToRefreshState?,
    isRefreshing: Boolean,
    onRefresh: (() -> Unit)?,
    content: TransformingLazyColumnScope.(TransformationSpec) -> Unit
) {
    val tlc = @Composable {
        TransformingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            state = state,
            contentPadding = PaddingValues(
                start = 10.dp,
                end = 10.dp,
                top = (if (isRound) 40.dp else 24.dp) + padding.calculateTopPadding(),
                bottom = (if (hasEdgeButton) 72.dp else (if (isRound) 48.dp else 24.dp)) + 
                        padding.calculateBottomPadding()
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            content(transformationSpec)
        }
    }

    if (pullToRefreshState != null && onRefresh != null) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            state = pullToRefreshState,
            modifier = Modifier.fillMaxSize(),
            content = { tlc() }
        )
    } else {
        tlc()
    }
}
