package dev.solsynth.solian.ui.scaffold

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.pointerInput
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.horizontalDrag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Wear OS swipe-back gesture composable.
 *
 * Detects left-edge horizontal swipe gestures and triggers [onBack] when the
 * swipe exceeds the threshold. Provides a natural follow-finger animation with
 * fade-out effect.
 *
 * Usage:
 * ```
 * SwipeBack(onBack = { navController.popBackStack() }) {
 *     // Your screen content
 * }
 * ```
 */
@Composable
fun SwipeBack(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    swipeThreshold: Dp = 80.dp,
    maxSwipeOffset: Dp = 300.dp,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val thresholdPx = swipeThreshold.toPx()
    val maxOffsetPx = maxSwipeOffset.toPx()

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                horizontalDrag(
                    onDrag = { dragAmount ->
                        scope.launch {
                            offsetX.snapTo((offsetX.value + dragAmount).coerceIn(0f, maxOffsetPx))
                        }
                    },
                    onDragEnd = {
                        scope.launch {
                            if (offsetX.value > thresholdPx) {
                                offsetX.animateTo(maxOffsetPx, animationSpec = tween(durationMillis = 150))
                                onBack()
                            } else {
                                offsetX.animateTo(0f, animationSpec = tween(durationMillis = 200))
                            }
                        }
                    },
                )
            }
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    translationX = offsetX.value,
                    alpha = 1f - (offsetX.value / maxOffsetPx) * 0.3f,
                )
        ) {
            content()
        }
    }
}
