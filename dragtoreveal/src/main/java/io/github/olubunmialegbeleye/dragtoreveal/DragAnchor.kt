package io.github.olubunmialegbeleye.dragtoreveal

internal sealed class DragAnchor {
    data object Resting : DragAnchor()  // 0px - nothing revealed
    data object Peeked : DragAnchor()  // -peekWidthPx - actions visible at rest
    data object Dismissed : DragAnchor()  // -fullWidthPx - item off-screen
}