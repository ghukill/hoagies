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
- 🔜 **Direct install: send the file, she taps to install** (decided 2026-11-11).
  - Release signing key, kept safe and backed up. Every update must be signed with the same
    key, or it won't install over the old version and her saved games are at risk.
  - Send via a Google Drive link rather than email; Gmail blocks `.apk` attachments.
  - A one-page, large-print guide for her, with screenshots of each prompt: allowing the
    install, the Play Protect warning, and switching "install unknown apps" back off afterwards.
  - Updates go out the same way and keep her progress (same key, higher version number).
  - Check Google's developer-verification rules for direct installs on Android 17 before
    relying on this long-term.
- 💡 **Plug-in updates through the iMac at her place (soon).** The goal: *"There's a new Hoagies
  version! Plug your phone into your iMac and I'll take it from there."*
  - One-time phone setup: turn on Developer options (tap Build number 7×), turn on USB
    debugging, plug into the iMac, tick "Always allow from this computer".
  - iMac: install `adb` and `scrcpy` (Homebrew, or standalone downloads if its macOS is too
    old for Homebrew). Needs a USB-A to USB-C *data* cable.
  - Updates: SSH into the iMac and run `adb install -r hoagies.apk`. No Drive link, no
    "unknown apps" prompt. Her saved games are kept (same signing key, higher version).
  - Remote help: run `scrcpy` over iMac screen sharing to see and control her phone, or forward
    adb over SSH so the phone appears as if plugged into this Mac (Claude can then check
    things on it like on the emulator, with her watching).
  - A small script that does the whole update in one step: build, copy to the iMac, install,
    open the app, take a screenshot as proof.
  - Good to know: she unlocks the phone herself; Android forgets the approval after about a
    week unused, so she may need to tap "Allow" again; USB debugging can be switched off
    between sessions for extra caution; adb over home Wi-Fi is possible later.
- 💡 **Remote help without the iMac:** TeamViewer QuickSupport or AnyDesk for when she's away
  from it.
  - **The golden rule** for her guide (and a sticky note): she only starts a remote session
    when *she* has called *you*. Nobody legitimate ever calls asking her to install a remote
    app or read out a code.
- 💡 Play Store, maybe later. It needs a newer target SDK, which means upgrading the build
  toolchain first.
