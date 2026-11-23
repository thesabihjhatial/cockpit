# Cockpit — Architecture

A single-module Android **car launcher** that replaces the stock home screen of generic
Android head units (target hardware: Yunovo NXOS units). It renders a fixed landscape
dashboard: clock and status chips, a now-playing panel, and a four-slot app dock, with an
app drawer that slides up from the bottom of the screen.

The design goals, in order, are: **glanceability** (readable at arm's length while driving),
**responsiveness** (every transition is under 150 ms), and **tolerance** of vendor firmware
that reports audio state in unusual ways.

---

## Quick facts

| | |
|---|---|
| Application ID / namespace | `com.thesabihjhatial.cockpit` |
| Modules | `:app` only |
| Language / UI | Kotlin, Jetpack Compose (foundation + ui only; no material3) |
| Gradle / AGP / Kotlin | 8.10.2 / 8.7.3 / 2.0.21 |
| Compose BOM | 2024.12.01 |
| Java target | 17 |
| `minSdk` / `targetSdk` / `compileSdk` | 26 / 35 / 35 |
| Orientation | landscape, locked |
| Resource configs | `en` only |
| Release build | R8 + resource shrinking, currently signed with the debug key |

Source volume: 53 Kotlin files / ~3,100 lines in `app/src/main`, plus 7 JVM unit-test files.

---

## Repository layout

```
app/src/main/
  AndroidManifest.xml          permissions, HOME activity, notification listener
  baseline-prof.txt            AOT profile (all app classes)
  java/com/thesabihjhatial/cockpit/
    MainActivity.kt            composition root
    apps/                      app enumeration, tiles, drawer sheet, dock launching
    bluetooth/                 connection + device name resolution
    clock/                     ticking time state
    header/                    greeting, time, date, status chips, expanded clock
    home/                      HomeScreen (all wiring) + HomeViewModel (apps & pins)
    media/                     now-playing pipeline, controls, visualizer
    network/                   Wi-Fi state
    permission/                runtime permission helper
    pins/                      dock slot persistence
    settings/                  intents to system settings screens
    splash/                    boot animation gate
    ui/                        theme, scaling, typography, icons, motion, gauge
  res/                         strings, colors, theme, Inter font, launcher icon
app/src/test/                  pure-JVM tests
docs/                          this document
test.html                      standalone 1280x720 browser mock of the same UI
```

---

## Composition root and startup

`MainActivity` does almost nothing but assemble the environment, in this order:

```
onCreate
  enableEdgeToEdge() + hideSystemBars()        immersive; re-hidden on every focus gain
  paintWindow()                               dark background before the first frame
  setContent {
    CockpitTheme {                            palette, text style and press tint
        DesignScale {                          overrides LocalDensity -> height / 720
          SplashGate(ready) { settled ->        boot animation; leaves when the scan is ready
            HomeScreen(settled, viewModel)      everything else happens here
          }
        }
      }
    }
  }
```

`HomeScreen` is the single integration point: it instantiates every effect (clock, Wi-Fi,
Bluetooth, media, permissions, dock state), owns the navigation state, and passes data down
to stateless composables. There is no dependency-injection framework, no navigation library,
and no `Application` subclass — the composition itself is the object graph.

As a `HOME` activity the app is declared with `launchMode="singleTask"`,
`clearTaskOnLaunch="true"`, `stateNotNeeded="true"` and `taskAffinity=""`. Pressing Home
while already running delivers `onNewIntent` with `CATEGORY_HOME`, which is the signal used
to collapse the drawer, expanded clock, and expanded player back to Home.

---

## Layered architecture

Three layers, applied consistently. The rule is: **Android APIs never leak past layer 1**.

### 1. Effect layer — Android callbacks → Compose state

Composables named `rememberX`, or plain functions returning `Flow<T>`, that wrap callbacks,
broadcasts, content observers and listeners. Lifecycle-aware collection via
`collectAsStateWithLifecycle`; `callbackFlow` + `awaitClose` for registration symmetry.

| Effect | Entry point | File |
|---|---|---|
| Ticking clock | `rememberNow()` | `clock/Now.kt` |
| Time/timezone changes | `timeChangeFlow()` | `clock/TimeChangeFlow.kt` |
| Bluetooth | `rememberBluetoothStatus()` | `bluetooth/BluetoothStatusState.kt` |
| Wi-Fi | `rememberWifiStatus()` | `network/WifiState.kt` |
| Now playing | `rememberMediaState()` | `media/MediaState.kt` |
| FFT spectrum | `rememberSpectrum()` | `media/Spectrum.kt` |
| Record-audio consent | `rememberSpectrumAccess()` | `media/SpectrumAccess.kt` |
| Installed apps | `HomeViewModel.apps` | `home/HomeViewModel.kt` |
| Runtime permissions | `rememberPermissions()` | `permission/PermissionAccess.kt` |

`Now.kt` is representative of the care taken here: it aligns ticks to the next minute
boundary instead of sleeping a fixed second, and it restarts the loop when the system clock
or timezone changes, so the displayed time can never drift.

### 2. Pure logic — no Android dependencies, unit-tested

Top-level functions with no framework types: `greetingLabel()`, `dateText()` / `timeText()`,
`formatTime()`, the pin-slot algebra
(`withFirstFree`, `withPinned`, `withoutId`, `withoutSlot`), `glyphCoverage()`, `levels()`
(FFT bins → bars), and `SheetState`'s drag / fling / snap state machine.

This layer exists so behaviour can be verified on the JVM without a device. Keep it pure;
if a function needs a `Context`, it belongs in layer 1.

### 3. Presentation — stateless composables

Take data plus `() -> Unit` callbacks, read colours from `LocalCockpitColors`, and contain no
Android API calls. All motion constants live in `ui/FadeThrough.kt` and each file's private
`const val`s so timings stay reviewable in one place.

---

## Screen state machine

Three screens, derived — never stored — from two booleans held in `HomeScreen`:

```
            tap clock                      tap now-playing / back
   Home ─────────────────► Clock     Home ─────────────────► NowPlaying
     ▲                       │          ▲                        │
     └─────── back ──────────┘          └──────── back ──────────┘
     │
     └── drawer (SheetState 0..1 over Home; back closes it first)

   screen = when {
       expanded && nowPlaying != null -> NowPlaying
       clockOpen                      -> Clock
       else                           -> Home
   }
```

Cross-fades use `AnimatedContent` with a shared-element transition scope. Two shared
elements carry continuity across screens:

- `ArtKey` — the album art grows from a dock-sized square to the 152 dp panel art.
- `TimeKey` — the clock text scales from 84 sp in the header to 300 sp on the clock screen
  (`sharedBounds` with `ScaleToBounds(FillWidth, CenterStart)`).

`BackHandler` is registered per state, in order of priority: drawer, then clock, then player.
The player also collapses automatically when playback ends (`nowPlaying == null`).

The app drawer is not a screen: it is an overlay whose progress is driven by `SheetState`
(0 = hidden, 1 = open) and which slides via `graphicsLayer`-equivalent `offset`, with its
background opacity following the same fraction. Grid overscroll is bridged into sheet drag
by a `NestedScrollConnection`, so a single continuous gesture scrolls the grid and then
dismisses the sheet.

---

## Layout and scaling

The home screen is a strict four-row grid, sized from the available height:

```
rowHeight = (maxHeight - 2 * 32dp - 3 * 16dp) / 4

┌──────────────────────────────────────────────┐
│ Header: greeting + time/date + status chips   │  row 1
├──────────────────────────────────────────────┤
│                                              │
│ Now playing: art | title/artist/progress | ▶  │  rows 2-3
│                                              │
├──────────────────────────────────────────────┤
│ Dock: 4 slots (app tile or "+ add app")       │  row 4
└──────────────────────────────────────────────┘
```

Everything is grid-fraction based rather than pixel based, so the layout is resolution
independent. Additionally, `DesignScale` replaces `LocalDensity` with
`density = constraints.maxHeight / 720` and `fontScale = 1f`. Two consequences worth
knowing:

1. Every `dp` and `sp` value is a **720p design-space unit**; the UI scales with the screen's
   height. A 1024x600 head unit renders the same composition, smaller.
2. The user's system font-size setting is deliberately ignored, so the fixed layout cannot
   break.

---

## Subsystems

### Apps and dock

- `launcherActivities()` collects `CATEGORY_LAUNCHER` **and** `CATEGORY_INFO` activities
  (the latter surfaces system utilities that hide from the launcher), excluding this package.
- `modeShortcuts()` adds vendor entry points that are not launcher activities — on Yunovo
  units, `cn.yunovo.nxos.activity.action.bt.music` and `...action.radio`. These are encoded as
  `component#action` ids (`ActionSeparator`), which `launchApp()` decodes back into an
  explicit `Intent(action).setComponent(...)`.
- Icons are normalised in `IconGlyph.kt`: adaptive icons are cropped from their 108 dp canvas
  to the 72 dp visible area, and a layer whose opaque coverage is under 40 % is treated as a
  monochrome glyph and rendered in white. This is what keeps the tiles legible on a wall of
  mismatched vendor icons.
- Tile colour comes from Palette's dominant colour, darkened in 6 % steps until it reaches
  3:1 contrast against white text (`TileColor.kt`), cached in-process by `id:lastModified`.
- The dock is four `String?` slots persisted newline-separated in shared preferences
  (`pins/PinnedStore.kt`). `HomeViewModel` resolves ids to `AppInfo` on every app-list change,
  with a fallback that matches the package part before `/` so pins saved by older builds
  (which stored bare package names) keep working.
- The app list refreshes on `PACKAGE_ADDED|REMOVED|REPLACED`, debounced 400 ms, re-read off
  the main thread (`Dispatchers.IO`, one coroutine per app for icon work).

### Media

The most involved subsystem, because a head unit can present playback three different ways
and only one of them is guaranteed. `MediaState` resolves a single `NowPlaying?` through a
priority chain:

```
1. MediaSession        MediaSessionManager.getActiveSessions(listenerComponent)
                       → prefers a session in STATE_PLAYING
                       requires notification-listener access
2. Legacy broadcasts   com.android.music.metachanged / playstatechanged
                       metadata carried in intent extras; playing/position tracked locally
3. Bluetooth stub      AudioManager.isMusicActive, held true for 30 s after the last sound
                       rendered as the connected device name with an "unknown track" title
```

`NowPlaying` is a single immutable snapshot (art, artist, controller, duration, position,
position-updated-at, playing, speed, source, title, has-next/previous). Position is
interpolated from `lastPositionUpdateTime * playbackSpeed` — never polled from the session —
and re-read every 500 ms only for display, which keeps the progress bar smooth without
waking the media stack.

Commands degrade gracefully: with a `MediaController` they call `TransportControls`;
without one they synthesise `KEYCODE_MEDIA_*` down/up pairs through `AudioManager`.

The visualizer (`Spectrum.kt`) uses `android.media.audiofx.Visualizer(0)` with a 256-byte
capture, folded into 21 logarithmically spaced bands with peak decay and a 1.5 s silence
cutoff. Each of the 21 bands is drawn as **21 stacked segments** — a 21x21 cell matrix, 3 dp apart
vertically — so the meter reads as discrete levels rather than a continuous bar. Colour is a
function of height, not of column: rows 0-6 are green (hue 120), rows 14-20 are red (hue 0), and
rows 7-13 carry the entire blend, so hue falls evenly across the middle third with yellow landing
on the centre row. Drawing is one `Path` per row across all columns, so it stays at 21 draw calls
per frame. When FFT data is unavailable — silence, no session, or a vendor stack that refuses
the visualizer — `VisualizerBars` animates a synthetic waveform instead, so the panel never
looks frozen while music plays. Bars are drawn in one reused `Path` inside a per-frame loop
driven by `withFrameNanos`, with exponential rise (50 ms) and fall (140 ms) envelopes.

### Bluetooth

The messiest code in the project, and unavoidably so: no single API reliably answers "is a
phone connected, and what is it called" across vendor firmware. The flow correlates four
independent sources and picks the first non-null name:

1. profile proxies (hidden profile ids 11 A2DP_SINK, 12 AVRCP_CONTROLLER, 16 HEADSET_CLIENT,
   17 PBAP_CLIENT, plus public A2DP/HEADSET) → `connectedDevices`
2. ACL connect/disconnect broadcasts → address → name map
3. bonded devices, filtered by a reflective `isConnected()` call
4. active audio devices of type A2DP or SCO

All four transitions funnel into a 500 ms `settle()` debounce, because vendor stacks emit
storms of partial state changes during a single connection. Every external call is wrapped
in `runCatching`, and the reflection is guarded.

### Network, display, clock

- Wi-Fi: `ConnectivityManager` network callback for a `TRANSPORT_WIFI` network; the SSID is
  read from `WifiManager.connectionInfo` and therefore needs fine-location permission.
- There is exactly one palette. Light mode was removed along with the second palette, the
  cross-fade and the brightness heuristic that chose between them: a head unit is a dark
  cabin, and `paintWindow()` and the window background now share the same constant.

### Permissions and settings

`rememberPermissions(*permissions)` returns a `Permissions` object whose `access(p)` exposes
granted state plus a request lambda. It handles three cases: normal request, permanently
denied (`shouldShowRequestPermissionRationale == false` → jumps to app settings), and a
tap-initiated request that escalates to app settings if the result comes back denied.
`BLUETOOTH_CONNECT` is treated as granted below API 31, where it does not exist.

---

## Design system

One `@Immutable data class CockpitColors` with 12 named roles (accent, accentInk,
accentSoft, background, line, muted, raised, ring, surface, text, text2, track) and exactly
one palette: near-black `#0F0F10` surfaces with a `#E5484D` accent. It is a `val` provided
through `LocalCockpitColors`, so nothing can switch it at runtime. Nothing in the app
hard-codes a colour except pure white on accent controls and tile icons.

Typography: one variable font, Inter, registered at five weights with
`FontVariation.Settings` so weight is interpolated rather than synthesised, and with
`fontFeatureSettings = "tnum, cv11"` applied globally — tabular numbers keep the clock from
jittering, and `cv11` gives the single-storey `a` the design calls for. The font file is a
**trimmed subset**, not the upstream release: `tools/subset_font.py` keeps only the
characters the UI can draw and limits the variable axes to `wght 400..800`, taking it from
857 KB to 131 KB. Anything outside that range — Cyrillic app names, CJK track titles — falls
back to the platform font, which is exactly the behaviour we want. Re-run the script after
adding strings with new characters; it fails loudly if a needed glyph, weight or feature
would be lost.

Icons are hand-authored vector paths in `CockpitIcons` (add, bluetooth, chevron, music note,
next, pause, play, previous, settings, wifi) parsed through `PathParser`, so there is no
Material icon dependency and stroke weights stay consistent at 2.2 dp.

Motion tokens (all in one place, `ui/FadeThrough.kt` unless noted):

| Token | Value | Used by |
|---|---|---|
| `FadeOutMs` | 37 ms | outgoing screen / overlay |
| `FadeInMs` | 88 ms | incoming screen |
| `FadeInDelayMs` | 37 ms | incoming screen (sequential, not overlapping) |
| `InitialScale` | 0.94 | incoming screen scale-in |
| sheet | 125 ms `FastOutSlowInEasing` | `SheetState`, splash enter/exit |
| position tick | 500 ms | progress bar |
| launch timeout | 10 s | spinner on a tapped tile |

The deliberately odd 62/63 ms asymmetry compensates for one frame of scheduling skew so the
veil reads as symmetric. `InitialScale` of 0.94 and the 37/88 split follow the same
principle: exits are faster than entrances, and nothing overlaps.

---

## Data and persistence

| Store | Keys | Contents |
|---|---|---|
| `SharedPreferences("pins")` | `slots` | four newline-separated app ids, empty string = free slot |
| `SharedPreferences("media")` | `spectrum_prompted` | whether the record-audio prompt was shown |
| In-memory | — | tile colour cache keyed `id:lastModified` (process lifetime) |

There is no database and no DataStore. All state is either persisted in those two preference
files or derived from the system at read time — deliberately, so the launcher always reflects
reality after a reboot of the head unit.

---

## Conventions

- **Naming encodes the layer.** `rememberX` = effect-producing composable; `xFlow` = cold
  `Flow` wrapper around a callback registration; `XState` / `XStatus` = data holder;
  `XStore` = persistence.
- **One `callbackFlow` per Android callback source**, always with `awaitClose` unregistering.
- **Constructors take callbacks, not interfaces.** `SheetState(scope) { onClosed }`,
  `TileAction(label, onSelect)`, `Permissions(granted, request, requestFromTap)`.
- **Constants at file top**, `private`, named in units where it matters (`...Ms`, `...Px`,
  `...Dp`), so timings and thresholds are auditable in one glance.
- **No `!!`**, no unchecked casts, `runCatching` around anything vendor-provided.
- **Strings in `res/values/strings.xml`**, with `contentDescription` on every icon that
  conveys meaning (the app is driven by touch while driving, so accessibility labels double
  as tap targets for testing).

---

## Testing

All tests are plain JVM tests (no Robolectric, no instrumentation), which is why the pure
layer is kept free of Android types. Coverage is focused on the things that are easy to get
subtly wrong:

| File | What it pins down |
|---|---|
| `header/GreetingTest` | greeting bucket boundaries (5/12/17/24) |
| `header/DateTimeTextTest` | `h:mm` without padding, 12-hour clock, `Thursday, 5 November` |
| `pins/PinnedStoreTest` | slot algebra: fill first free, move, replace, no-op cases |
| `apps/SheetStateTest` | fling up/down, snap at 50 %, drag consumption (fake frame clock) |
| `apps/IconGlyphTest` | opaque-coverage ratio used for the monochrome-glyph decision |

Not covered (needs a device or a larger harness): the media pipeline, Bluetooth name
resolution, app enumeration, and the vendor shortcut decoding.

```bash
./gradlew testDebugUnitTest          # unit tests
./gradlew assembleDebug              # debug APK
./gradlew assembleRelease            # minified; note: currently debug-signed
./gradlew installDebug               # to a connected head unit / emulator
```

`test.html` is a standalone 1280x720 browser mock of the same interface, useful for iterating
on layout and timings without a device. It duplicates the token values and animation curves
by hand, so **it is a reference, not a source of truth** — when you change a timing or a
colour, change it in both or delete it.

---

## Constraints and known sharp edges

1. **Release signing is opt-in.** `assembleRelease` reads `keystore.properties`
   (gitignored, see `keystore.properties.example`) and signs with that key; with no file it
   falls back to the debug key so the build still works, but such a build cannot replace an
   installed copy. `versionCode` is still hand-written, so two builds are indistinguishable
   in the field until that is automated.
2. **Vendor coupling.** `cn.yunovo.*` permissions and the two NXOS mode actions only resolve
   on Yunovo hardware. On other devices the dock simply shows fewer entries; nothing crashes.
3. **`QUERY_ALL_PACKAGES`** is required to enumerate the full app grid. This would prevent a
   Play Store listing, which is acceptable for a sideloaded system launcher.
4. **Hidden APIs.** Bluetooth profile ids 11/12/16/17 and the reflective
   `BluetoothDevice.isConnected()` are not public surface and can change; all uses are
   guarded, and the fallback chain degrades to bonded/audio-device names.
5. **Splash lingers on slow scans.** `SplashGate` dismisses once the first app scan reports
   ready, but never before the 1.2 s logo animation finishes and never later than 3 s. On a
   cold boot with a cold package cache the shortcut is the ceiling: the launcher appears with
   a partially filled grid rather than not at all.
6. **No predictive back.** `android:enableOnBackInvokedCallback` is not set, so system back
   animations are the legacy ones; all handling is `BackHandler`.
7. **`rememberWifiStatus(locationGranted)`** uses the flag only as a `remember` key, so the
   Wi-Fi flow and its SSID read run whether or not location is granted (the SSID will simply
   be null without it). Cosmetic, but misleading to read.
8. **No diagnostics surface.** The build ships no debug UI and no event log; every effect is
   read-only and silent. Debugging a vendor device means adding temporary instrumentation,
   not flipping a switch.

---

## Size and startup budget

The target is a low-spec head unit, so package size and cold start are treated as
requirements. Measure before and after any dependency or asset change.

Release APK: **0.880 MB**, down from 1.45 MB. Composition of the compressed payload:

| Part | Compressed | Share | Notes |
|---|---|---|---|
| `classes.dex` | 864 KB | 94% | Compose runtime + ui + foundation only, R8-minified |
| `res/font/…ttf` | 33 KB | 4% | 199 codepoints, `wght 400..800`, `tnum` + `cv11` only |
| `resources.arsc` | 11 KB | 1% | |
| manifest, baseline profile, misc | 9 KB | 1% | |

Applied: `isMinifyEnabled` + `isShrinkResources`, `-repackageclasses ''` +
`-allowaccessmodification`, `resourceConfigurations = ["en"]`, `dependenciesInfo`
excluded, `kotlin/**` and `META-INF` dropped from the package, the native
`libandroidx.graphics.path.so` excluded from all four ABIs, the trimmed font, and —
the largest single change — **material3 removed entirely** in favour of five small
first-party widgets in `ui/` (`Text`, `Icon`, `CircularProgressIndicator`, `DropdownMenu`,
`PressIndication`). Those keep material3's parameter lists so call sites were unchanged.

Dex is now 94% of the APK, and what remains is Compose runtime + foundation + our own
~3,600 lines. Further size work would mean giving up Compose itself, which is not worth
it; the remaining levers are `androidx.palette` (~8 KB, needs a hand-rolled dominant-colour
pass) and per-ABI split APKs.

Startup costs after this work, in the order they bite on slow hardware:

1. `AppRepository.loadApps` — one bitmap decode per installed app, two only for an app (or app
   version) seen for the first time, four at a time. Palette extraction, previously the
   dominant cost at ~80 analyses per cold start, now runs once per app version and is served
   from `SharedPreferences("tile-colors")` thereafter. Every app is still decoded eagerly even
   though only four dock slots and one grid screen are visible; making icon decode lazy is the
   remaining known win, and needs a visual check on a device.
2. Per-glyph variable-font instancing in Skia (reduced, but not eliminated, by the axis trim).
3. The splash no longer defines time-to-usable: it leaves as soon as the first scan reports
   ready, with a 1.2 s floor so the logo animation completes and a 3 s ceiling.

Rule of thumb for future changes: no new dependency without a measured line in this table,
and no asset in `res/` that is not referenced by code.

