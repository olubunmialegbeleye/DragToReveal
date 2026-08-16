package io.github.olubunmialegbeleye.dragtoreveal

import androidx.compose.runtime.Immutable

sealed class DragAnchor {
    @Immutable
    data object Resting : DragAnchor()  // 0px - nothing revealed
    @Immutable data object Peeked : DragAnchor()  // -peekWidthPx - actions visible at rest
    @Immutable data object Dismissed : DragAnchor()  // -fullWidthPx - item off-screen
}