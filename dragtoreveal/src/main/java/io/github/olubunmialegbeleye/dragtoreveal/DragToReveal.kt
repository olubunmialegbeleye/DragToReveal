@file:OptIn(ExperimentalFoundationApi::class)

package io.github.olubunmialegbeleye.dragtoreveal

import android.annotation.SuppressLint
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private const val DEFAULT_POSITIONAL_THRESHOLD = 0.5f

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun<T> DragToReveal(
    actions: PersistentList<RevealAction>,
    itemKey: T,
    revealState: DragToRevealState<T>,
    modifier: Modifier = Modifier,
    initialAnchor: DragAnchor = DragAnchor.Resting,
    enabled: Boolean = true,
    fullDismissEnabled: Boolean = true,
    positionalThreshold: (totalDistance: Float) -> Float = { it * DEFAULT_POSITIONAL_THRESHOLD },
    velocityThreshold: Dp = 125.dp,
    animationSpec: AnimationSpec<Float> = spring(),
    defaultActionKey: String = actions.last().key,
    content: @Composable (() -> Unit),
) {
    if (actions.isEmpty()) {
        content()
        return
    }

    val defaultAction =
        remember(actions, defaultActionKey) {
            actions.find { it.key == defaultActionKey }
        }

    val currentDefaultAction by rememberUpdatedState(defaultAction)

    requireNotNull(defaultAction) {
        "defaultActionKey '$defaultActionKey' does not match any action key. Available keys: ${actions.map { it.key }}"
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        val fullDismissPx = constraints.maxWidth.toFloat()

        val (peekWidthPx, state) = rememberDragStateHolder(
            itemKey = itemKey,
            actions = actions,
            fullDismissPx = fullDismissPx,
            fullDismissEnabled = fullDismissEnabled,
            isRtl = isRtl,
            positionalThreshold = positionalThreshold,
            velocityThreshold = velocityThreshold,
            animationSpec = animationSpec,
            initialAnchor = initialAnchor,
        )

        DragToRevealEffects(
            state = state,
            itemKey = itemKey,
            revealState = revealState,
            currentDefaultAction = currentDefaultAction,
        )

        val scope = rememberCoroutineScope()
        val currentOffset = if (state.offset.isNaN()) 0f else state.offset
        val revealedPx = abs(currentOffset)

        RevealBackground(
            actions = actions,
            revealedPx = revealedPx,
            peekWidthPx = peekWidthPx,
            defaultActionKey = defaultActionKey,
            onActionClick = { onClick ->
                onClick()
                scope.launch { state.animateTo(DragAnchor.Resting) }
            },
            modifier =
                Modifier
                    .align(if (isRtl) Alignment.CenterStart else Alignment.CenterEnd)
                    .fillMaxHeight(),
        )

        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(if (state.offset.isNaN()) 0 else state.offset.roundToInt(), 0) }
                    .anchoredDraggable(state, Orientation.Horizontal, enabled = enabled),
            color = Color.Transparent,
        ) {
            content()
        }
    }
}

@Composable
private fun<T> DragToRevealEffects(
    state: AnchoredDraggableState<DragAnchor>,
    itemKey: T,
    revealState: DragToRevealState<T>,
    currentDefaultAction: RevealAction?,
) {
    LaunchedEffect(state) {
        snapshotFlow { state.currentValue }
            .distinctUntilChanged()
            .collectLatest { anchor ->
                when (anchor) {
                    DragAnchor.Resting -> revealState.onItemClosed(itemKey)
                    DragAnchor.Peeked -> revealState.onItemOpened(itemKey)
                    DragAnchor.Dismissed -> {
                        // unreachable when fullDismissEnabled = false (anchor not registered)
                        currentDefaultAction?.onClick()
                        state.snapTo(DragAnchor.Resting)
                    }
                }
            }
    }

    LaunchedEffect(revealState.openItemKey) {
        if (revealState.openItemKey != itemKey && state.currentValue != DragAnchor.Resting) {
            state.animateTo(DragAnchor.Resting)
        }
    }
}

@Composable
internal fun RevealBackground(
    actions: PersistentList<RevealAction>,
    revealedPx: Float,
    peekWidthPx: Float,
    defaultActionKey: String,
    onActionClick: (onClick: () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (revealedPx <= 0f) return
    val density = LocalDensity.current

    val phase1Ratio = (revealedPx / peekWidthPx).coerceIn(0f, 1f)
    val isExpanded = revealedPx > peekWidthPx

    val nonDefaultPeekPx =
        remember(actions, defaultActionKey, density) {
            actions
                .filter { it.key != defaultActionKey }
                .sumOf { with(density) { it.width.toPx().toDouble() } }
                .toFloat()
        }

    Row(
        modifier =
            modifier
                .width(with(density) { revealedPx.toDp() }),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        actions.forEach { action ->
            key(action.key) {
                val isDefault = action.key == defaultActionKey

                val slotWidthPx =
                    when {
                        !isExpanded -> with(density) { action.width.toPx() } * phase1Ratio
                        !isDefault -> with(density) { action.width.toPx() }
                        else -> revealedPx - nonDefaultPeekPx
                    }

                val slotState = RevealActionSlotState(
                    isDefault = isDefault,
                    revealFraction = phase1Ratio,
                    isExpanded = isExpanded,
                )

                Box(
                    modifier =
                        Modifier
                            .width(with(density) { slotWidthPx.toDp() })
                            .fillMaxHeight()
                            .clickable(
                                onClickLabel = action.contentDescription,
                                onClick = { onActionClick(action.onClick) },
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    action.content(slotState)
                }
            }
        }
    }
}

private val previewActions = persistentListOf(
    RevealAction(
        key = "Delete",
        contentDescription = "Delete",
        onClick = {},
        content = { slotState ->
            Text(
                text = "Delete",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = slotState.revealFraction),
            )
        },
    ),
)

@Preview
@Composable
private fun PreviewDragToRevealResting() {
    DragToReveal(
        actions = previewActions,
        itemKey = "1",
        revealState = rememberDragToRevealState(),
    ) {
        Text(text = "Item to Drag", style = MaterialTheme.typography.titleMedium)
    }
}

@Preview
@Composable
private fun PreviewDragToRevealPeeked() {
    DragToReveal(
        actions = previewActions,
        itemKey = "1",
        initialAnchor = DragAnchor.Peeked,
        revealState = rememberDragToRevealState(),
    ) {
        Text(text = "Item to Drag", style = MaterialTheme.typography.titleMedium)
    }
}
