package io.github.olubunmialegbeleye.dragtoreveal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class DragToRevealState<T> {
    var openItemKey: T? by mutableStateOf(null)
        private set

    fun onItemOpened(key: T) {
        openItemKey = key
    }

    fun onItemClosed(key: T) {
        if (openItemKey == key) openItemKey = null
    }


    fun closeAll() {
        openItemKey = null
    }
}

@Composable
fun <T> rememberDragToRevealState(): DragToRevealState<T> = remember { DragToRevealState() }
