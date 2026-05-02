package io.github.olubunmialegbeleye.dragtoreveal

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class RevealAction(
    val key: String,
    val width: Dp = 65.dp,
    val backgroundColor: Color,
    val contentDescription: String,
    val onClick: () -> Unit,
    val content: @Composable () -> Unit,
)
