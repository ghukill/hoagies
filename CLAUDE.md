# Hoagies — agent notes

Android (Kotlin, Jetpack Compose, Material 3) collection of classic games for older players;
see README.md for layout and the "adding a game" recipe.

## Build
- Needs JDK 17: `export JAVA_HOME=/opt/homebrew/opt/openjdk@17` before `./gradlew` (system default is JDK 11).
- `./gradlew testDebugUnitTest assembleDebug` must pass before calling work done.

## Non-negotiables
- Never add network, ads, analytics, tracking, or purchase libraries. Don't remove the
  `tools:node="remove"` permission entries from AndroidManifest.xml.
- Universal design: tappable things ≥ 64dp (`BigTouchTarget`); buttons carry text labels, not
  just icons; every destructive action goes through `ConfirmDialog`; meaningful
  `contentDescription`s; no time pressure by default; plain, warm wording ("Start over", not "Reset").
- Saved-game ids (`SudokuGame.ID`, …) and Room tables are player data: never rename them; bump the
  DB version with a real Migration (schemas are exported to `app/schemas/`), never destructive migration.
- Never edit a shipped Mahjong board layout (saved games map tiles by position index); add a new one.
- Keep game rules in pure Kotlin `engine/` packages with unit tests; UI state in ViewModels.
- Lobbies reuse `ui/components/GameLobby.kt` (ContinueCard, ChoiceCard, ProgressCard, HowToPlayDialog)
  and play screens use `games/common/GameClock`.
- Plans and ideas live in `docs/ROADMAP.md`; keep it current when features land.
- Libraries are pinned in `gradle/libs.versions.toml`; DI is the hand-rolled `AppContainer` (no Hilt).
