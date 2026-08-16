package io.github.olubunmialegbeleye.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.olubunmialegbeleye.dragtoreveal.DragToReveal
import io.github.olubunmialegbeleye.dragtoreveal.RevealAction
import io.github.olubunmialegbeleye.dragtoreveal.rememberDragToRevealState
import io.github.olubunmialegbeleye.sample.ui.theme.DragToRevealTheme
import kotlinx.collections.immutable.persistentListOf

class SampleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DragToRevealTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        Surface(shadowElevation = 4.dp) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 48.dp, bottom = 16.dp, start = 16.dp)
                            ) {
                                Text(
                                    text = "Drag to Reveal Samples",
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    SampleList(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

data class SampleItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: ItemType,
    var isFavorite: Boolean = false
)

enum class ItemType {
    SWIPE_TO_DISMISS,
    ONE_MENU,
    TWO_MENU,
    DYNAMIC
}

@Composable
fun SampleList(modifier: Modifier = Modifier) {
    val items = remember {
        mutableStateListOf(
            SampleItem("1", "Swipe to Dismiss", "Native-like swipe to delete", ItemType.SWIPE_TO_DISMISS),
            SampleItem("2", "One Menu", "Simple single action reveal", ItemType.ONE_MENU),
            SampleItem("3", "Two Menus", "Reveal multiple actions", ItemType.TWO_MENU),
            SampleItem("4", "Dynamic Menu", "Actions change based on state", ItemType.DYNAMIC),
            SampleItem("5", "Archive", "Another swipe to dismiss", ItemType.SWIPE_TO_DISMISS),
            SampleItem("6", "Quick Action", "One menu example", ItemType.ONE_MENU),
            SampleItem("7", "Settings", "Two menus example", ItemType.TWO_MENU),
            SampleItem("8", "Interactive", "Dynamic menu example", ItemType.DYNAMIC),
        )
    }

    val revealState = rememberDragToRevealState<String>()

    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(items, key = { it.id }) { item ->
            val actions = when (item.type) {
                ItemType.SWIPE_TO_DISMISS -> {
                    persistentListOf(
                        RevealAction(
                            key = "delete",
                            contentDescription = "Delete",
                            onClick = { items.remove(item) },
                            content = {
                                ActionIcon(
                                    icon = Icons.Default.Delete,
                                    modifier = Modifier.background(color = Color(0xFFE53935))
                                )
                            }
                        )
                    )
                }
                ItemType.ONE_MENU -> {
                    persistentListOf(
                        RevealAction(
                            key = "share",
                            contentDescription = "Share",
                            onClick = { /* Handle share */ },
                            content = { ActionIcon(
                                icon = Icons.Default.Share,
                                modifier = Modifier.background(color = Color(0xFF1E88E5))
                            ) }
                        )
                    )
                }
                ItemType.TWO_MENU -> {
                    persistentListOf(
                        RevealAction(
                            key = "more",
                            contentDescription = "More",
                            onClick = { /* Handle more */ },
                            content = {
                                ActionIcon(
                                    icon = Icons.Default.MoreVert,
                                    modifier = Modifier.fillMaxSize().background(color = Color(0xFF757575))
                                )
                            }
                        ),
                        RevealAction(
                            key = "email",
                            contentDescription = "Email",
                            onClick = { /* Handle email */ },
                            content = { ActionIcon(
                                icon = Icons.Default.Email,
                                modifier = Modifier.fillMaxSize().background(color = Color(0xFF43A047))
                            )
                            }
                        )
                    )
                }
                ItemType.DYNAMIC -> {
                    persistentListOf(
                        RevealAction(
                            key = "favorite",
                            contentDescription = "Favorite",
                            onClick = { 
                                val index = items.indexOf(item)
                                if (index != -1) {
                                    items[index] = item.copy(isFavorite = !item.isFavorite)
                                }
                            },
                            content = { 
                                ActionIcon(
                                    icon = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    modifier = Modifier.fillMaxSize().background(color = if (item.isFavorite) Color(0xFFFFB300) else Color(0xFFFB8C00))
                                )
                            }
                        ),
                        RevealAction(
                            key = "delete",
                            contentDescription = "Delete",
                            onClick = { items.remove(item) },
                            content = { ActionIcon(
                                icon = Icons.Default.Delete,
                                modifier = Modifier.fillMaxSize().background(color = Color(0xFFE53935))
                            ) }
                        )
                    )
                }
            }

            DragToReveal(
                actions = actions,
                itemKey = item.id,
                revealState = revealState,
            ) {
                ItemContent(item)
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
        }
    }
}

@Composable
fun ActionIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = Color.White,
        modifier = modifier//.size(24.dp)
    )
}

@Composable
fun ItemContent(item: SampleItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.title.first().toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (item.isFavorite) {
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SampleListPreview() {
    DragToRevealTheme {
        SampleList()
    }
}
