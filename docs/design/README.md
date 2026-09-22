# Handoff: Atmospheric — Android app UI (OPSC6312 Part 2)

## Overview
Atmospheric is a Kotlin/Android weather and environmental tracking app with an offline-first
architecture (Retrofit + Open-Meteo → Repository → Room → ViewModel → UI, WorkManager for
background sync, FCM for severe-weather alerts, DataStore for preferences, multilingual UI in
English / isiZulu / Afrikaans).

This bundle documents the UI for five screens: **Login**, **Dashboard**, **Forecast & Graph**,
**Sync & Offline**, and **Settings**. The repository `ST10443034/atmospheric` already contains
`LoginActivity`, `DashboardActivity` and `SettingsActivity` with plain ConstraintLayout/LinearLayout
XML; Forecast and Sync/Offline do not exist yet and must be added.

## About the Design Files
The files in this bundle are **design references created in HTML** — interactive prototypes showing
intended look and behaviour. They are **not** production code to port line by line.

The task is to recreate these designs **in the existing Android codebase**, using its established
patterns: Kotlin, MVVM, view binding, Material 3 (`Theme.Material3.DayNight.NoActionBar`), and the
existing `res/layout` XML approach (or Jetpack Compose if the team decides to migrate — but XML
matches what is already there).

The HTML prototype is NOT in the GitHub repo — that is why Claude Code cannot find it. Drop this
whole folder into the repo (suggested path: `docs/design/`) and commit it, then point Claude Code at
`docs/design/README.md`.

## Fidelity
**High-fidelity.** Colours, type sizes, spacing, radii and all copy are final. Recreate the UI
faithfully, but express it through Material 3 components (`MaterialCardView`,
`TextInputLayout`, `MaterialSwitch`, `TabLayout`, `BottomNavigationView`) rather than hand-rolled
views. The prototype's fonts are a web substitute — on Android use the platform default
(Roboto) or bundle a similar geometric sans; do not try to match the web fonts exactly.

## Design Tokens

### Colour — dark theme (default)
| Token | Value | Use |
| --- | --- | --- |
| `bg` | `#0B1218` | screen background |
| `nav_bg` | `#0D151C` | bottom navigation bar |
| `panel` | `#FFFFFF` @ 5.5% | cards, chips, inputs, segmented backgrounds |
| `line` | `#FFFFFF` @ 10% | card borders, dividers |
| `line_strong` | `#FFFFFF` @ 22% | outlined button borders |
| `ink` | `#E8EEF2` | primary text |
| `ink_muted` | `ink` @ 60–70% | labels, secondary text |
| `accent` | `#1D6D99` | filled buttons, switch ON track |
| `link` | `#6FB8E4` | links, active nav/tab, live indicator |
| `glyph` | `#3D5A72` | weather icon placeholder circles |
| `warn` | `#DFA869` | cached/offline state, queue badges |
| `error` | `#E08B6F` | validation text, logout, error titles |
| `error_border` | `#B4583F` | error state input/card border |
| `nav_inactive` | `#5B6B77` | inactive bottom-nav item |

### Colour — light theme
| Token | Value |
| --- | --- |
| `bg` | `#F4F6F7` |
| `nav_bg` | `#E9EDF0` |
| `panel` | `#10202D` @ 5% |
| `line` | `#10202D` @ 12% |
| `line_strong` | `#10202D` @ 28% |
| `ink` | `#101D26` |
| `accent` | `#16648F` |
| `link` | `#14618D` |
| `glyph` | `#9FB2C1` |
| `warn text` | `#8A5A10` |
| `nav_inactive` | `#8496A4` |

Status-pill fills: online `#56AEE4` @ 13% fill / 38% stroke; offline `#DFA869` @ 14% fill / 42% stroke.

### Spacing
Screen padding 20dp (Login 26dp). Vertical section gaps 16 / 18 / 20 / 22dp.
Card padding 13dp (metric tiles) and 17dp (status card). Grid/row gaps 8–10dp.

### Type scale (sp on Android)
| Role | Size | Weight |
| --- | --- | --- |
| Hero temperature | 88 | 300 |
| Screen title | 19 | 600 |
| Section heading | 15 | 600 |
| Body | 14.5 | 400 |
| Condition line | 17 | 400 |
| Secondary body | 13–13.5 | 400 |
| Monospace label (all-caps, letter-spacing 0.08–0.1em) | 10–12 | 400–500 |

Monospace is used deliberately for machine-ish values: coordinates, timestamps, queue IDs,
API variable names, DataStore state. Use a monospace typeface for these.

### Radii & sizing
Cards 14dp · segmented/tab containers 12dp (inner selected 9dp) · inputs & buttons 12dp ·
pills 999dp (fully rounded) · brand tile 22dp.
Inputs and filled buttons 52–54dp tall. All tappable rows ≥48dp. Switch 50×30dp, knob 24dp.

## Screens

### 1. Login / SSO
**Purpose:** email + password auth, Google SSO, create-account entry point.

Layout, top to bottom, centred column, 26dp horizontal padding:
- 30dp top margin, then brand tile 78×78dp, radius 22dp, gradient `160deg, #2B4A63 → #13212C`,
  1dp `line` border, containing a 30dp `#DFA869` circle (the sun; the shipped launcher icon
  replaces this).
- "Atmospheric" 26sp/600, letter-spacing −0.01em.
- "Your weather, even when you're offline." 13.5sp, `ink` @ 66%, centred.
- 38dp gap, then the form, 13dp between fields:
  - `EMAIL` label — 10.5sp monospace, letter-spacing 0.1em, `ink` @ 66%; input 52dp, `panel` fill,
    1dp `line`, radius 12dp, 15dp horizontal padding, placeholder `you@example.com`.
  - `PASSWORD` label + masked input, same spec, placeholder `••••••`.
  - Error line 12.5sp `error`, shown under the fields.
  - Filled button 54dp, `accent` fill, white 16sp/600 text — "Log in" / "Signing in…" while busy.
  - `OR` divider: 1dp `line` rules either side of a 10.5sp monospace label.
  - Outlined button 54dp, 1dp `line_strong`, "Continue with Google" with a 20dp circle placeholder
    (replace with the Google `SignInButton` already in `activity_login.xml`).
- Pinned to the bottom: "No account? **Create one**" 13.5sp, the second half in `link`.

No bottom navigation on this screen.

### 2. Dashboard / current weather
**Purpose:** current conditions for the saved location, with an unambiguous live-vs-cached signal.

- Header row: location "Johannesburg" 19sp/600 with `-26.2041, 28.0473` 10.5sp monospace below
  (left); status pill (right) — dot + `LIVE` or `CACHED`, 10sp monospace, tinted fill/stroke per
  token table. In the prototype the pill is tappable to fake the network; in the app it is read-only.
- **Loading** (`DashboardUiState.Loading`): centred 30dp circular progress + a 11sp monospace
  caption, 60dp below the header.
- **Success** (`DashboardUiState.Success`):
  - 88sp/300 temperature with the unit symbol at 25sp/300, `ink` @ 60%, baseline-offset 8dp.
  - Condition 17sp; "Feels like 21° · 24° / 11°" 13.5sp @ 65%.
  - Freshness line 10.5sp monospace in `link` (online) or `warn` (offline):
    `LAST UPDATED 08:10 · LIVE` / `CACHED 06:42 · OFFLINE SINCE 08:15`.
  - Three equal metric tiles (HUMIDITY / WIND / PRECIP), 9dp gap: 9.5sp monospace caption @ 60%,
    value 21sp below. Values from `CurrentWeather.humidity`, `.windSpeed`, `.precipitation`.
  - "Hourly forecast" 15sp/600 with a `SEE ALL` monospace link (navigates to Forecast).
  - Horizontal strip of 5 hour cards (`RecyclerView`, matches `item_hourly_forecast.xml`):
    radius 13dp, `panel` fill, 1dp `line`, 11dp/5dp padding, stacked time (10sp monospace @ 60%) /
    15dp `glyph` circle / temperature 14.5sp.
- **Error** (`DashboardUiState.Error`): card with 1dp `error_border`, title "Could not load weather
  data" 15sp/600 `error`, body "No network and no cached forecast in Room yet. Connect and retry."
  13sp @ 70%, then a 46dp outlined "Retry" button.

### 3. Forecast & graph
**Purpose:** hourly list, 7-day list, and a graph of a chosen Open-Meteo variable.

- Title "Forecast" 19sp/600.
- Segmented tab control (use `TabLayout`): container `panel`, radius 12dp, 5dp padding;
  three equal tabs 13.5sp, selected tab gets a `#FFFFFF` @ 11% fill and radius 9dp.
- **Hourly tab:** rows 13dp vertical, separated by 1dp `line`:
  time (46dp wide, 12sp monospace @ 65%) · 14dp `glyph` circle · condition (13sp @ 70%, fills) ·
  precipitation probability (12sp monospace `link`, 44dp) · temperature (14.5sp, 46dp, right-aligned).
- **Daily tab:** same row grammar, 14dp vertical: day `MON` · circle · condition · pop ·
  `24° / 11°` (66dp, right-aligned). Seven rows.
- **Graph tab:**
  - Four filter chips — Temperature, Precip %, Humidity, Wind — 8/13dp padding, radius 999dp;
    selected chip `#56AEE4` @ 16% fill, 42% stroke, `link` text; unselected transparent with a
    `line` stroke.
  - Graph card: radius 16dp, `panel` fill, 1dp `line`, 17/15dp padding. Header row = variable name
    (`TEMPERATURE_2M · 24H`, 10sp monospace @ 60%) and freshness on the right.
    Plot area 130dp tall with three 1dp horizontal gridlines in `line`; series is a 2.5dp
    round-joined polyline, `#56AEE4` when live and `#DFA869` when cached. Axis labels
    `00 06 12 18 24`, 9.5sp monospace @ 50%. Footer: current value 24sp/300 plus
    "now · 14° – 25°" 12.5sp @ 60%.
  - Below the card, the request shape as a 10.5sp monospace note:
    `GET /v1/forecast?latitude=-26.2041&longitude=28.0473&timezone=auto`.
  - On Android draw this with MPAndroidChart or a custom `View`; the point of the design is one
    series, no chart junk, colour carrying the freshness signal.

### 4. Sync & offline
**Purpose:** make data freshness and the pending `SyncQueue` legible, per the offline-first strategy.

- Title "Sync & offline" 19sp/600.
- Status card, radius 16dp, tinted fill + stroke (online blue / offline amber), 17dp padding:
  9dp dot + state name 16sp/600 in the tint colour; blurb 13sp @ 75%
  ("Connected. WorkManager runs a periodic CoroutineWorker to refresh the Room cache." /
  "Showing cached forecast. Queued operations run when a network becomes available.");
  then a 10.5sp monospace stats row `CACHED 06:42 · PENDING 3 · RETRIES 2`.
- Two side-by-side 50dp buttons, 9dp gap: outlined "Retry now" and a `panel`-filled
  "Go offline"/"Go online" (the latter is prototype-only — remove it in the app).
- "Queued operations" 15sp/600 with a `SYNC_QUEUE` monospace tag on the right.
- Queue cards, 9dp apart, radius 14dp, `panel` fill, 1dp `line`, 13/14dp padding:
  operation name 12sp monospace, status pill 9.5sp monospace (amber tint for PENDING/RETRYING,
  blue tint for SYNCING), then `ID 0142 · 08:18 · RETRY 0` at 10sp monospace @ 55%.
  Fields map 1:1 to `SyncQueueEntity` (queueId, operation, status, retryCount, createdAt).
- Empty state: dashed 1dp `line` box, radius 14dp, 26dp padding, centred
  "Queue empty — everything is synced." 13.5sp @ 60%.

### 5. Settings / language
**Purpose:** DataStore-backed preferences — theme, units, language, notifications, logout.

- Title "Settings" (localised) 19sp/600.
- Group labels 10sp monospace all-caps, letter-spacing 0.1em, @ 55%: `THEME`, `UNITS`, `LANGUAGE`.
- Theme: segmented control Light / Dark / System (replaces the existing Spinner).
- Units: segmented control "Metric (°C)" / "Imperial (°F)". Changing it converts every temperature
  and wind value in the app.
- Language: three stacked rows, radius 12dp, ≥48dp, 13/14dp padding — English, isiZulu, Afrikaans.
  Selected row gets the `panel` fill, a `line_strong` border and a `SELECTED` 10sp monospace tag.
- Notifications row between 1dp `line` rules, 16dp vertical: "Enable notifications" 14.5sp with
  "FCM severe-weather alerts" 11.5sp @ 60% beneath; Material switch on the right — ON track
  `accent`, knob white.
- "Log out" 14.5sp `error`, 17dp vertical padding.
- Diagnostic footer, 10sp monospace @ 45%: `DataStore · theme=Dark units=Metric lang=English`.
  Keep it behind a debug flag or drop it in release.

### Bottom navigation (all screens except Login)
`BottomNavigationView`, 1dp `line` top border, `nav_bg` fill, 9dp top / 12dp bottom padding,
four equal items: Home, Forecast, Offline, Settings. Icon slot 20dp (replace the prototype's
rounded squares with real icons), label 11sp. Active item uses `link`; the Offline tab turns `warn`
when the device is offline and carries a count badge (min 16dp circle, `#DFA869` fill,
`#10161C` text, 9.5sp monospace) when the queue is non-empty. Inactive items use `nav_inactive`.

## Interactions & Behaviour

**Login**
- Validation mirrors `LoginViewModel.validateInput`: invalid/blank email →
  "Invalid email address"; password shorter than 6 → "Password must be at least 6 characters".
  Errors render inline under the fields, not as a Toast.
- Valid submit → `LoginUiState.Loading` (button reads "Signing in…") → `Success` → Dashboard.
- Google SSO → Firebase Auth → Dashboard.

**Dashboard**
- On entry, load weather for the saved location (repo default: -26.2041, 28.0473, locationId 1).
- Online: fetch from Open-Meteo, cache in Room, render `LIVE` + a fresh timestamp.
- Offline: render the newest Room row, pill `CACHED`, freshness line names the offline-since time.
  If Room is empty, show the Error state.

**Forecast**
- Tab and chip selection are local UI state; data comes from the same cached `WeatherInfo`.
- Graph series recolours to amber when the data shown is cached.

**Sync & offline**
- Losing connectivity enqueues `REFRESH_FORECAST`, `UPDATE_PREFERENCES`, `SYNC_LOCATIONS` as
  PENDING rows.
- "Retry now" while offline → rows become RETRYING, retryCount increments, snackbar
  "No network — worker deferred with back-off."
- Regaining connectivity → snackbar "Back online — WorkManager will sync shortly.", the worker runs,
  rows go SYNCING (~900ms) then clear, cached timestamp updates, snackbar "Sync complete. Forecast
  refreshed."
- Empty queue + Retry → "Nothing to sync."

**Settings**
- Theme applies immediately (`AppCompatDelegate.setDefaultNightMode`).
- Units convert in place — no reload.
- Language switches the in-app locale only (per-app locales / `AppCompatDelegate.setApplicationLocales`),
  not the device language.
- Logout clears the session and returns to Login with the password field cleared.

**Feedback:** the prototype's toast is a 10dp-radius `#1C2731` panel, 13sp text, 96dp above the
bottom edge, auto-dismissing after ~2.4s. On Android use a `Snackbar` anchored above the nav bar.

**Transitions:** none beyond the platform defaults. Loading indicator ~750ms in the prototype; use
real request timing.

## State Management

Follow the existing MVVM split.

- `LoginViewModel` — `LoginUiState`: Idle / Loading / Success / Error(message). Already implemented.
- `DashboardViewModel` — `DashboardUiState`: Loading / Success(WeatherInfo) / Error(message).
  Needs an added notion of data freshness: whether the emitted `WeatherInfo` came from the network
  or from Room, plus its timestamp. Recommended: give `WeatherInfo` an `isCached: Boolean` and
  `fetchedAt: Long`, and make `WeatherRepository.getWeatherForecast` emit the mapped Room entity on
  failure instead of `null` (the current code has a `// Defer:` comment exactly here — this design
  depends on that fallback existing).
- **New** `SyncViewModel` — exposes connectivity state, cached timestamp, and a
  `Flow<List<SyncQueueEntity>>` from `WeatherDao`; `retryNow()` enqueues a one-time
  `OneTimeWorkRequest` with a `NetworkType.CONNECTED` constraint and exponential back-off.
- **New** `ForecastViewModel` (or reuse `DashboardViewModel`) — holds selected tab and selected
  graph variable, derives series from the cached `WeatherInfo`.
- `SettingsViewModel` — already exposes `themeMode`, `units`, `notificationsEnabled` from DataStore;
  add `language`. All are `StateFlow` with DataStore as the source of truth.

Data fetching: Retrofit `OpenMeteoApiService.getForecast(lat, lon)` → `WeatherDto.toDomainModel()`
→ cache via `WeatherDao.insertWeather` → emit. Room queries return `Flow` so the UI reacts to
cache writes.

## Assets
No bitmap assets in this bundle. Every weather icon and nav icon in the prototype is a placeholder
circle or rounded square; supply real icons in the app (Material Symbols, or a weather-code icon set
mapped from `weatherCode`). The launcher icon in the repo (`ic_launcher_foreground.xml`) stays as is.
Strings for isiZulu and Afrikaans in the prototype are unverified translations — have a native
speaker review them before they go into `values-zu/strings.xml` and `values-af/strings.xml`.

## Files
| File | What it is |
| --- | --- |
| `Atmospheric Prototype.dc.html` | The interactive prototype — open in a browser. All five screens, real navigation, validation, network toggle, theme/unit/language switching. Source of truth for behaviour. |
| `Atmospheric Screens.dc.html` | Static side-by-side view of the five screens, matching the Part 1 Planning & Design document. |
| `android-frame.jsx` | Device bezel used by the prototypes. Not app code. |
| `support.js` | Runtime the prototype HTML needs to render. Not app code. |

Open `Atmospheric Prototype.dc.html` directly; keep all four files in the same folder.
