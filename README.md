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
- **Generic item keys** — `itemKey`/`DragToRevealState` work with any stable key type, not just `String`
- **Variable actions** — one action or many; each with its own width and composable content
- **Default action** — the primary/destructive action stretches in phase 2 and fires on a full swipe
- **Optional full dismiss** — set `fullDismissEnabled = false` to cap the gesture at the Peeked anchor (menu-only, no swipe-to-delete)
- **Configurable gesture feel** — override `positionalThreshold`, `velocityThreshold`, and `animationSpec` per instance
- **RTL aware** — reveal direction follows `LocalLayoutDirection` automatically
- **Mutual exclusion** — opening one item in a list automatically closes all others
- **Reveal-fraction driven styling** — action content receives a `RevealActionSlotState` (`isDefault`, `revealFraction`, `isExpanded`) so colour/alpha/scale live in your composable instead of being imposed by the library
- **Haptic feedback** — a confirmation tick fires when the drag crosses into the Dismissed anchor
- **Tap outside to close** — tapping the item content while open closes it
- **Zero recomposition during drag** — offset changes run entirely in the draw phase
- **Accessibility** — `contentDescription` drives TalkBack labels, plus a `stateDescription` (Expanded/Collapsed) and per-action `CustomAccessibilityAction`s so switch/TalkBack users can trigger actions without dragging
- **Disableable** — pass `enabled = false` to freeze the gesture (e.g. while a row is mid-animation elsewhere)

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
    val revealState = rememberDragToRevealState<String>()

    LazyColumn {
        items(messages, key = { it.id }) { message ->
            DragToReveal(
                itemKey     = message.id,
                revealState = revealState,
                actions     = persistentListOf(
                    RevealAction(
                        key                = "mark_read",
                        contentDescription = "Mark as read",
                        onClick            = { /* handle */ },
                        content            = { slotState ->
                            Icon(
                                Icons.Default.Check, null,
                                tint = Color.White,
                                modifier = Modifier.background(Color(0xFF5B2D8E)),
                            )
                        },
                    ),
                    RevealAction(
                        key                = "delete",
                        contentDescription = "Delete",
                        onClick            = { /* handle */ },
                        content            = { slotState ->
                            Icon(
                                Icons.Default.Delete, null,
                                tint = Color.White,
                                modifier = Modifier.background(Color(0xFFE05252)),
                            )
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
            contentDescription = "Delete",
            onClick            = { viewModel.delete(item) },
            content            = { slotState ->
                Icon(
                    Icons.Default.Delete, null,
                    tint = Color.White,
                    modifier = Modifier.background(Color(0xFFE05252)),
                )
            },
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

`RevealAction.content` is a free composable slot that receives a `RevealActionSlotState` — render anything inside it, and apply your own background/tint:

```kotlin
RevealAction(
    key                = "delete",
    width              = 80.dp,   // wider to accommodate the label
    contentDescription = "Delete",
    onClick            = { viewModel.delete(item) },
    content            = { slotState ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE05252)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(Icons.Default.Delete, null, tint = Color.White)
            Spacer(Modifier.height(4.dp))
            Text("Delete", color = Color.White, fontSize = 11.sp)
        }
    },
)
```

### Reacting to `RevealActionSlotState`

Each action's `content` is invoked with a `RevealActionSlotState(isDefault, revealFraction, isExpanded)`. Use it to fade non-default actions in during phase 1, or restyle the default action once it stretches past the peek width in phase 2 — the library no longer imposes an alpha/colour treatment for you:

```kotlin
content = { slotState ->
    Icon(
        Icons.Default.Delete,
        null,
        tint = Color.White,
        modifier = Modifier
            .background(Color(0xFFE05252))
            .alpha(if (slotState.isDefault) 1f else slotState.revealFraction),
    )
}
```

### Menu-only rows (no full dismiss)

Set `fullDismissEnabled = false` to cap the drag at the Peeked anchor — useful when actions shouldn't be triggerable by a fast fling, only by tapping:

```kotlin
DragToReveal(
    itemKey             = item.id,
    revealState         = revealState,
    fullDismissEnabled  = false,
    actions             = actions,
) { ItemRow(item) }
```

---

## API reference

### `DragToReveal`

```kotlin
@Composable
fun <T> DragToReveal(
    actions: PersistentList<RevealAction>,
    itemKey: T,
    revealState: DragToRevealState<T>,
    modifier: Modifier = Modifier,
    initialAnchor: DragAnchor = DragAnchor.Resting,
    enabled: Boolean = true,
    fullDismissEnabled: Boolean = true,
    positionalThreshold: (totalDistance: Float) -> Float = { it * 0.5f },
    velocityThreshold: Dp = 125.dp,
    animationSpec: AnimationSpec<Float> = spring(),
    defaultActionKey: String = actions.last().key,
    content: @Composable () -> Unit,
)
```

| Parameter | Description |
|---|---|
| `actions` | Ordered list of action slots. Put the primary/destructive action last. |
| `itemKey` | Stable unique key of any type `T` — must match the key used in your `LazyColumn` and the `T` of your `DragToRevealState`. |
| `revealState` | Shared state from `rememberDragToRevealState<T>()`. Create at list level. |
| `modifier` | Applied to the outermost container. |
| `initialAnchor` | Anchor the row starts at, e.g. `DragAnchor.Peeked` to render pre-opened. |
| `enabled` | When `false`, freezes the drag gesture and clears custom accessibility actions. |
| `fullDismissEnabled` | When `false`, the `Dismissed` anchor is never registered — the gesture caps at `Peeked`. |
| `positionalThreshold` | Fraction of the gap between anchors a drag must cross (by distance) to settle on the next anchor, as in `AnchoredDraggableState`. |
| `velocityThreshold` | Fling speed above which a drag skips ahead to the next anchor regardless of `positionalThreshold`. |
| `animationSpec` | Spring/tween used for `animateTo`/snap-back animations. |
| `defaultActionKey` | Key of the stretching/full-swipe action. Defaults to `actions.last().key`. |
| `content` | The foreground composable — your list item body. |

---

### `RevealAction`

```kotlin
data class RevealAction(
    val key: String,
    val width: Dp = 65.dp,
    val contentDescription: String,
    val onClick: () -> Unit,
    val content: @Composable (RevealActionSlotState) -> Unit,
)
```

| Parameter | Description |
|---|---|
| `key` | Stable unique identifier for this action. |
| `width` | Fixed slot width. The default action stretches beyond this in phase 2. |
| `contentDescription` | Accessibility label exposed to TalkBack, and the label for this action's `CustomAccessibilityAction`. |
| `onClick` | Fires when the slot is tapped, or when this is the default and a full swipe completes. |
| `content` | Composable rendered inside the slot, given the current `RevealActionSlotState`. Apply background colour/tint here — the library no longer paints a background or alpha for you. |

---

### `RevealActionSlotState`

```kotlin
@Immutable
data class RevealActionSlotState(
    val isDefault: Boolean,
    val revealFraction: Float,
    val isExpanded: Boolean,
)
```

| Field | Description |
|---|---|
| `isDefault` | Whether this slot is the `defaultActionKey` action. |
| `revealFraction` | `0f`–`1f` progress through phase 1 (Resting → Peeked). Clamped once phase 2 begins. |
| `isExpanded` | `true` once the drag has passed the Peeked anchor — the default action is stretching in phase 2. |

---

### `DragToRevealState`

```kotlin
@Stable
class DragToRevealState<T> {
    val openItemKey: T?
    fun closeAll()
}

@Composable
fun <T> rememberDragToRevealState(): DragToRevealState<T>
```

Holds the key of whichever item is currently open. `null` when no item is open. Create one instance per list using `rememberDragToRevealState<T>()` and pass it to every `DragToReveal` in that list. Call `closeAll()` to programmatically collapse the open row (e.g. on scroll or navigation).

---

## Gesture behaviour

```
Settled ──────────────────────── 0px
                                   │
         user swipes left          │  release < positionalThreshold → snaps back
                                   ▼
Peeked ───────────────── -peekWidthPx   (sum of all action widths)
                                   │
         user continues dragging   │  release < positionalThreshold → snaps back
                                   ▼  fling ≥ velocityThreshold → skips to Dismissed
Dismissed ───────────── -fullWidthPx   (container width)
                                   │
                                   ├─ haptic tick fires on entering this anchor
                                   └─ fires defaultAction.onClick()
                                      snaps back to Settled
```

Direction mirrors under RTL layouts (`LocalLayoutDirection`) — offsets above are for LTR.

If `fullDismissEnabled = false`, the `Dismissed` anchor is never registered and the drag caps at `Peeked`.

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
| RTL support | ✅ | ✅ |
| Optional full dismiss | ❌ | ✅ (`fullDismissEnabled`) |
| Custom accessibility actions | ❌ | ✅ |

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
