# Hoagies roadmap

What we've built, what's in progress, and what we want to build. Newest ideas go at the
bottom of their section; move things up as they firm up.

**Status key:** ✅ done · 🚧 in progress · 🔜 next up · 💡 idea

---

## Games

| Status | Game | Notes |
|---|---|---|
| ✅ | **Sudoku** | Four levels (Gentle → Hard), notes, hints, undo, mistake highlighting, autosave, stats. |
| 🚧 | **Mahjong Solitaire** | Tap-two-to-match, stacked tiles. Four boards (Meadow, Diamond, Pyramid, Fortress), every deal winnable, hint / shuffle / undo, "Simple shapes" tile set. |
| 💡 | Solitaire (Klondike) | Big cards, tap-to-move as well as drag, unlimited undo. |
| 💡 | Word Search | Large letters, tap first and last letter instead of dragging. |
| 💡 | Crosswords | Needs a source of puzzles we're allowed to ship. |

### Mahjong Solitaire follow-ups
- 💡 **Custom tile sets.** The loader is ready: drop 42 images into
  `app/src/main/assets/mahjong/tilesets/<name>/` (see the README there) and they appear under
  Settings → Mahjong Solitaire → Tile style.
- 💡 More boards; maybe a "Daily board".
- 💡 A gentle animation as matched tiles disappear.
- 💡 Record shuffles in results (needs a database migration).
- 💡 Version board layouts inside saved games, so changing a shipped layout can't scramble
  someone's game in progress. Until then: never edit a shipped layout, add a new one.

---

## App-wide features

### 🔜 Break reminder ("make a sandwich")
A playful, gentle nudge after a long session. Tongue-in-cheek, but also kind of real.

- After **60 minutes** of the app being open and in use, show:
  > **You've been really going at it!**
  > Maybe take a break and make a sandwich? 🥪
- Repeats **every 60 minutes** after that.
- Gentle: a soft card or dialog with one friendly button ("Good idea!") — never blocks the
  game, never loses progress, no sound, no countdown.
- Counts only time the app is actually on screen (not while it's in the background).
- A Settings switch to turn it off (on by default).
- Open questions:
  - Should time away (say 15+ minutes in the background) reset the hour?
  - Should the clock pause in the middle of a game while the message is showing?

### 💡 Other ideas
- Onboarding: a one-time, skippable "here's how Hoagies works" for first launch.
- Read-aloud for instructions (Android text-to-speech, works offline).
- Optional sounds (off by default), with a volume setting.

---

## Quality & accessibility
- 💡 Full TalkBack pass on every screen.
- 💡 Check every screen at the largest phone font size plus "Extra large" in the app.
- 💡 Landscape and tablet layout pass.
- 💡 Move user-facing text into `strings.xml` so the app can be translated.

## Getting it onto phones
- 💡 Final app name and icon.
- 💡 Release signing key, plus simple instructions for installing on a family member's phone.
- 💡 Decide on Play Store vs. direct install. The Play Store needs a newer target SDK, which
  means upgrading the build toolchain first.
