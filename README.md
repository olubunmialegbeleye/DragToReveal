# DragToReveal

A three-anchor swipe-to-reveal component for Jetpack Compose. Matches the interaction model of iOS `swipeActions` — items slide left to reveal contextual actions, rest at a midpoint anchor, and fully dismiss with a colour-flood confirmation signal.

> **Status:** `0.1.0` — API is still settling. Minor versions may introduce breaking changes until `1.0.0`.

---

<!--

## Preview

Replace with an actual screen recording GIF once available
![DragToReveal demo](docs/demo.gif)

---

-->

## Features

- **Three named anchors** — Resting, Peeked (actions visible), and Dismissed
- **Variable actions** — one action or many; each with its own width, colour, and composable content
- **Default action** — the primary/destructive action stretches in phase 2 and fires on a full swipe
- **Mutual exclusion** — opening one item in a list automatically closes all others
- **Colour-flood signal** — non-default actions dim when a full swipe is imminent, matching iOS UX
- **Tap outside to close** — tapping the item content while open closes it
- **Zero recomposition during drag** — offset changes run entirely in the draw phase
- **Accessibility** — each action slot exposes a TalkBack click label via `contentDescription`

---

## Installation

### Option A — JitPack (recommended, no auth required)

Add JitPack to your root `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add the dependency:

```kotlin
// build.gradle.kts (app or feature module)
dependencies {
    implementation("com.github.olubunmialegbeleye:DragToReveal:v0.1.0")
}
```

### Option B — GitHub Packages

Add the repository to your root `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            name = "GitHubPackages"
            url  = uri("https://maven.pkg.github.com/olubunmialegbeleye/DragToReveal")
            credentials {
                username = providers.gradleProperty("githubUsername").orNull
                password = providers.gradleProperty("githubToken").orNull
            }
        }
    }
}
```

Add your GitHub credentials to `~/.gradle/gradle.properties` (global, not in your project):

```properties
githubUsername=your_github_username
githubToken=your_pat_with_read_packages_scope
```

Add the dependency:

```kotlin
dependencies {
    implementation("io.github.olubunmialegbeleye:dragtoreveal:0.1.0-alpha02")
}
```

### Version catalog (`libs.versions.toml`)

```toml
[versions]
dragtoreveal = "0.1.0-alpha02"

[libraries]
dragtoreveal = { group = "io.github.olubunmialegbeleye", name = "dragtoreveal", version.ref = "dragtoreveal" }
```

```kotlin
// build.gradle.kts
dependencies {
    implementation(libs.dragtoreveal)
}
```

---

## Requirements

| Requirement | Minimum |
|---|---|
| Android | API 24 (Android 7.0) |
| Kotlin | 1.9.0 |
| Jetpack Compose BOM | 2024.02.00 |
| `kotlinx-collections-immutable` | 0.3.7 |

---

## Quick start

```kotlin
@Composable
fun NotificationList(messages: List<Message>) {
    // Create ONE shared state for the entire list.
    // This is what enforces mutual exclusion — do not
    // move this inside the item composable.
    val revealState = rememberDragToRevealState()

    LazyColumn {
        items(messages, key = { it.id }) { message ->
            DragToReveal(
                itemKey     = message.id,
                revealState = revealState,
                actions     = persistentListOf(
                    RevealAction(
                        key                = "mark_read",
                        backgroundColor    = Color(0xFF5B2D8E),
                        contentDescription = "Mark as read",
                        onClick            = { /* handle */ },
                        content            = {
                            Icon(Icons.Default.Check, null, tint = Color.White)
                        },
                    ),
                    RevealAction(
                        key                = "delete",
                        backgroundColor    = Color(0xFFE05252),
                        contentDescription = "Delete",
                        onClick            = { /* handle */ },
                        content            = {
                            Icon(Icons.Default.Delete, null, tint = Color.White)
                        },
                    ),
                ),
            ) {
                // Your list item content goes here
                MessageRow(message)
            }
        }
    }
}
```

---

## Usage guide

### Single action

When there is only one action, it is automatically the default — no configuration needed.

```kotlin
DragToReveal(
    itemKey     = item.id,
    revealState = revealState,
    actions     = persistentListOf(
        RevealAction(
            key                = "delete",
            backgroundColor    = Color(0xFFE05252),
            contentDescription = "Delete",
            onClick            = { viewModel.delete(item) },
            content            = { Icon(Icons.Default.Delete, null, tint = Color.White) },
        ),
    ),
) {
    ItemRow(item)
}
```

### Overriding the default action

By default, the **last** action in the list is the default. It stretches to fill the row in phase 2 and fires on a full swipe. To override this, pass `defaultActionKey` explicitly:

```kotlin
DragToReveal(
    itemKey          = item.id,
    revealState      = revealState,
    defaultActionKey = "archive",   // first action is now the default
    actions          = persistentListOf(
        RevealAction(key = "archive", ...),
        RevealAction(key = "snooze", ...),
    ),
) { ... }
```

### Conditional actions

Build the action list dynamically based on item state. Wrap in `remember` so the list is not reallocated on every recomposition:

```kotlin
val actions = remember(item.id, item.isDeletable) {
    buildList {
        add(RevealAction(key = "mark_read", ...))
        if (item.isDeletable) {
            add(RevealAction(key = "delete", ...))
        }
    }.toImmutableList()
}

DragToReveal(
    itemKey     = item.id,
    revealState = revealState,
    actions     = actions,
) { ... }
```

### Icon + label layout

`RevealAction.content` is a free composable slot — render anything inside it:

```kotlin
RevealAction(
    key                = "delete",
    width              = 80.dp,   // wider to accommodate the label
    backgroundColor    = Color(0xFFE05252),
    contentDescription = "Delete",
    onClick            = { viewModel.delete(item) },
    content            = {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Delete, null, tint = Color.White)
            Spacer(Modifier.height(4.dp))
            Text("Delete", color = Color.White, fontSize = 11.sp)
        }
    },
)
```

---

## API reference

### `DragToReveal`

```kotlin
@Composable
fun DragToReveal(
    actions: PersistentList<RevealAction>,
    itemKey: String,
    revealState: DragToRevealState,
    modifier: Modifier = Modifier,
    defaultActionKey: String = actions.last().key,
    content: @Composable () -> Unit,
)
```

| Parameter | Description |
|---|---|
| `actions` | Ordered list of action slots. Put the primary/destructive action last. |
| `itemKey` | Stable unique key — must match the key used in your `LazyColumn`. |
| `revealState` | Shared state from `rememberDragToRevealState()`. Create at list level. |
| `modifier` | Applied to the outermost container. |
| `defaultActionKey` | Key of the stretching/full-swipe action. Defaults to `actions.last().key`. |
| `content` | The foreground composable — your list item body. |

---

### `RevealAction`

```kotlin
data class RevealAction(
    val key: String,
    val width: Dp = 65.dp,
    val backgroundColor: Color,
    val contentDescription: String,
    val onClick: () -> Unit,
    val content: @Composable () -> Unit,
)
```

| Parameter | Description |
|---|---|
| `key` | Stable unique identifier for this action. |
| `width` | Fixed slot width. The default action stretches beyond this in phase 2. |
| `backgroundColor` | Slot background colour. |
| `contentDescription` | Accessibility label exposed to TalkBack. |
| `onClick` | Fires when the slot is tapped, or when this is the default and a full swipe completes. |
| `content` | Composable rendered inside the slot. Typically an `Icon`. |

---

### `DragToRevealState`

```kotlin
@Stable
class DragToRevealState {
    val openItemKey: String?
}

@Composable
fun rememberDragToRevealState(): DragToRevealState
```

Holds the key of whichever item is currently open. `null` when no item is open. Create one instance per list using `rememberDragToRevealState()` and pass it to every `DragToReveal` in that list.

---

## Gesture behaviour

```
Settled ──────────────────────── 0px
                                   │
         user swipes left          │  release < 50% of gap → snaps back
                                   ▼
Peeked ───────────────── -peekWidthPx   (sum of all action widths)
                                   │
         user continues dragging   │  release < 50% of gap → snaps back
                                   ▼  fast fling (≥ 200dp/s) → skips to Dismissed
Dismissed ───────────── -fullWidthPx   (screen width + 200dp buffer)
                                   │
                                   └─ fires defaultAction.onClick()
                                      snaps back to Settled
```

---

## Common mistakes

**Creating `DragToRevealState` inside the item composable**

```kotlin
// ❌ Each item gets its own isolated state — mutual exclusion is broken
@Composable
fun MyListItem(item: Item) {
    val revealState = rememberDragToRevealState()  // wrong placement
    DragToReveal(revealState = revealState, ...) { ... }
}

// ✅ One shared instance for the whole list
@Composable
fun MyList(items: List<Item>) {
    val revealState = rememberDragToRevealState()  // correct placement
    LazyColumn {
        items(items, key = { it.id }) { item ->
            MyListItem(item = item, revealState = revealState)
        }
    }
}
```

**Building the action list without `remember`**

```kotlin
// ❌ New list object on every recomposition — invalidates all remember blocks
DragToReveal(
    actions = persistentListOf(RevealAction(...)),
    ...
)

// ✅ Stable reference
val actions = remember(item.id, item.isDeletable) {
    buildList { ... }.toImmutableList()
}
DragToReveal(actions = actions, ...)
```

---

## Comparison with SwipeToDismissBox

| Feature | `SwipeToDismissBox` | `DragToReveal` |
|---|---|---|
| Midpoint resting anchor | ❌ | ✅ |
| Actions stay visible after release | ❌ | ✅ |
| Variable number of actions | ❌ | ✅ |
| Per-action composable content | ❌ | ✅ |
| Mutual exclusion across list | ❌ | ✅ |
| Colour-flood confirmation signal | ❌ | ✅ |
| Tap outside to close | ❌ | ✅ |
| Haptic feedback | ❌ | ✅ |

---

## Contributing

Contributions are welcome. Please open an issue before submitting a pull request for new features — this keeps the scope aligned and avoids duplicate work.

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature-name`
3. Make your changes in the `dragtoreveal` module
4. Add or update the sample app to demonstrate the change
5. Open a pull request against `main`

---

## License

```
Copyright 2026 Olubunmi Alegbeleye

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
