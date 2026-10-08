# Atlas — Native Android App (Java)

This is a **real native Android app**, written in Java, not a WebView wrapper
around the HTML file. It reimplements the core of your Atlas world-building
tool as native Android screens, and stores everything locally on-device
using `SharedPreferences` (no server, no cloud, no WebView storage tricks).

## What's included

- **Bookshelf** — create, rename, delete, and open books (worlds).
- **Wiki tab** — grid of entries across the six categories (Characters,
  Locations, Factions, Items, Events, Concepts), each with its category
  color, a search box, and category filter chips.
- **Entry editor** — name, category, tags, summary, description, any number
  of custom key/value fields, and links to other entries in the same book
  (kept bidirectional, like the original — linking A to B also links B to A).
- **Story tab** — a plain full-screen text editor for your running draft,
  with a live word count, autosaved to local storage as you type.

## What's not included (scope trimmed for a native rewrite)

The original HTML app also had a relationship-tree visualization, an
interactive world map with pins, background themes, and ambient radio
streaming. Rebuilding all of that natively is a much larger project (custom
canvas drawing, image pickers, streaming audio, etc.), so this version
focuses on the core wiki + story workflow, which is the heart of the app.
If you want any of those ported natively next, let me know which one matters
most and I'll build it.

## Where your data lives

Everything is stored in a single `SharedPreferences` file
(`atlas_prefs`, key `atlas_books_json`) as JSON — see
`app/src/main/java/com/atlas/bookwriter/data/Storage.java`. There's no
network code anywhere in the app. Uninstalling the app or clearing its
storage from Android settings will erase your books, same as any other
locally-stored app.

## Project structure

```
app/src/main/java/com/atlas/bookwriter/
  model/      Book, Entry, Field, Category — plain data classes with JSON (de)serialization
  data/       Storage — the only place that touches SharedPreferences
  ui/
    BookshelfActivity   — home screen, list of books
    BookAdapter
    BookActivity        — one book: Wiki tab + Story tab
    EntryAdapter
    EntryEditActivity   — create/edit a single wiki entry
```

## How to build it

You'll need [Android Studio](https://developer.android.com/studio) (or the
Android SDK + a JDK) on a machine with normal internet access — Gradle needs
to download the Android Gradle Plugin and AndroidX libraries from Google's
Maven repo, which wasn't reachable in the sandbox this was built in.

### Option A — Android Studio
1. Unzip this project.
2. **Open** the `AtlasNative` folder in Android Studio.
3. Let Gradle sync (first sync needs internet).
4. Click **Run ▶** with a device/emulator connected, or
   **Build → Build Bundle(s)/APK(s) → Build APK(s)**.

### Option B — Command line
```bash
cd AtlasNative
./gradlew assembleDebug
```
The debug APK lands at:
`app/build/outputs/apk/debug/app-debug.apk`

Install it with:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Customizing

- **App icon**: replace `app/src/main/res/drawable/ic_launcher_foreground.xml`
  and `ic_launcher_background.xml`, or use Android Studio's *Image Asset*
  tool (right-click `res` → New → Image Asset) for your own artwork.
- **App name**: `app/src/main/res/values/strings.xml` → `app_name`.
- **Colors/theme**: `app/src/main/res/values/colors.xml` and `themes.xml`
  carry over the ink/parchment/gold palette from the original design.
- **Package ID**: `com.atlas.bookwriter`, set in `app/build.gradle`
  (`namespace` and `applicationId`) — change before publishing if needed.
