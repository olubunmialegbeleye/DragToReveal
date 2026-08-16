package io.github.olubunmialegbeleye.dragtoreveal

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import kotlinx.collections.immutable.PersistentList

internal data class DragStateHolder @OptIn(ExperimentalFoundationApi::class) constructor(
    val peekWidthPx: Float,
    val state: AnchoredDraggableState<DragAnchor>,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun <T> rememberDragStateHolder(
    itemKey: T,
    actions: PersistentList<RevealAction>,
    fullDismissPx: Float,
    fullDismissEnabled: Boolean,
    isRtl: Boolean,
    positionalThreshold: (Float) -> Float,
    velocityThreshold: Dp,
    animationSpec: AnimationSpec<Float>,
    initialAnchor: DragAnchor,
): DragStateHolder {
    val density = LocalDensity.current
    val peekWidthPx = remember(actions, density) {
        actions.sumOf { with(density) { it.width.toPx().toDouble() } }.toFloat()
    }
    val anchors = remember(actions, density, fullDismissPx, fullDismissEnabled, isRtl) {
        val sign = if (isRtl) 1f else -1f
        DraggableAnchors {
            DragAnchor.Resting at 0f
            DragAnchor.Peeked at sign * peekWidthPx
            if (fullDismissEnabled) DragAnchor.Dismissed at sign * fullDismissPx
        }
    }
    val state = remember(itemKey) {
        AnchoredDraggableState(
            initialValue = initialAnchor,
            positionalThreshold = positionalThreshold,
            velocityThreshold = { with(density) { velocityThreshold.toPx() } },
            snapAnimationSpec = animationSpec,
            decayAnimationSpec = exponentialDecay(),
        )
    }
    SideEffect { state.updateAnchors(anchors) }
    return DragStateHolder(peekWidthPx = peekWidthPx, state = state)
}
