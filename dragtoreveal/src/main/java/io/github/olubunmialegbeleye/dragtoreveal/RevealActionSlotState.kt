package io.github.olubunmialegbeleye.dragtoreveal

import androidx.compose.runtime.Immutable

@Immutable
data class RevealActionSlotState(
    val isDefault: Boolean,
    val revealFraction: Float,
    val isExpanded: Boolean,
)
