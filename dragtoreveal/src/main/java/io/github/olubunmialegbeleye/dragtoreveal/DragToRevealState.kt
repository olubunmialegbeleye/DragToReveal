package io.github.olubunmialegbeleye.dragtoreveal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class DragToRevealState {
    var openItemKey: String? by mutableStateOf(null)
        private set

    fun onItemOpened(key: String) {
        openItemKey = key
    }

    fun onItemClosed(key: String) {
        if (openItemKey == key) openItemKey = null
    }
}