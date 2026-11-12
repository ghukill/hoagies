# Hoagies

Classic games, safely. One Android app with familiar favourites — **no ads, no purchases,
no internet, no tracking, no pressure** — built first for older players who've been burned by
predatory "free" games and malware look-alikes, and designed so everyone else enjoys it too.

Games so far: **Sudoku** and **Mahjong Solitaire**. More to follow — see
[docs/ROADMAP.md](docs/ROADMAP.md).

## Principles

- **Safe by construction.** The manifest strips `INTERNET`, network-state and ad-ID permissions
  with `tools:node="remove"`, so even a future dependency can't quietly add them.
- **Universal design.** Big touch targets (64dp), larger base type plus an in-app text-size
  setting on top of the phone's own, high-contrast themes, labelled buttons (never icon-only),
  TalkBack descriptions for every Sudoku square, a confirmation before anything destructive,
  and no timers or countdowns unless you switch them on.
- **Built-in memory.** Games save automatically and resume where you left off; finished
  games are recorded for "your progress" stats. All of it stays on the phone (and in the
  phone's own backup).

## Build & run

Requirements: JDK 17 and the Android SDK (Gradle fetches missing SDK platforms itself).

```sh
export JAVA_HOME=/opt/homebrew/opt/openjdk@17   # or any JDK 17
./gradlew testDebugUnitTest      # engine + rules tests
./gradlew assembleDebug          # app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug           # onto a connected phone or running emulator
```

`local.properties` (not committed) needs `sdk.dir=/path/to/Android/sdk`.

To install on a real phone: enable *Developer options → USB debugging*, plug in, and
`./gradlew installDebug`. Release builds (`assembleRelease`) are minified but unsigned — add a
signing config before distributing.

## Layout

```
app/src/main/java/com/hoagies/games/
├── HoagiesApp.kt, AppContainer.kt   app + hand-rolled dependency container
├── MainActivity.kt                   theme + navigation host
├── data/
│   ├── settings/                     AppSettings + DataStore repository
│   └── memory/                       Room DB: saved games (JSON per game) + results
├── games/
│   ├── GameRegistry.kt               the list of games shown on the home screen
│   └── sudoku/
│       ├── engine/                   pure Kotlin: grid, solver, generator, game rules
│       └── ui/                       lobby, play screen, board, controls, view models
└── ui/                               theme, shared components, home, settings, navigation
```

## Adding a game

1. Create `games/<name>/` with a pure-Kotlin `engine/` (rules + a `@Serializable` state class)
   and a `ui/` package.
2. Persist with `GameMemory.save(ID, serializer, state)` / `load(...)`, and call
   `recordResult(...)` when a game ends. Pick a stable `ID` — it keys the player's history.
3. Add routes to `ui/navigation/Routes.kt` and `HoagiesNavHost.kt`.
4. Add a `GameInfo` to `GameRegistry.games`.

## Mahjong Solitaire notes

Every deal can be cleared: the dealer "plays" the empty board first, removing random free
pairs, then assigns matching faces to those pairs. Shuffle re-deals the remaining tiles the
same way. Boards live in `games/mahjong/engine/MahjongLayouts.kt`.

Tile artwork is swappable: drop images into `app/src/main/assets/mahjong/tilesets/<name>/`
(file names in the README there) and the set appears in Settings. Missing faces fall back to
the built-in "Simple shapes".

## Sudoku notes

Puzzles are generated on-device with a guaranteed unique solution, removing clues in
symmetric pairs. Difficulty is graded by the techniques needed, not just clue count:
*Gentle* needs only "one number fits here", *Easy/Medium* add "this number fits only here",
*Hard* can need anything.
