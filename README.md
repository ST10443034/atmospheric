# Atmospheric

OPSC6312 — Group 24. An offline-first Android weather app.

## Overview

Atmospheric fetches current, hourly and daily forecasts from the
[Open-Meteo](https://open-meteo.com/) API, caches them in a local Room
database, and keeps working when the network doesn't: a `WeatherRepository`
falls back to the last cached reading whenever a live fetch fails, and a
Sync & offline screen makes the pending sync queue and connectivity state
visible to the user.

Built with Kotlin, MVVM, view binding, Jetpack Navigation (a single-Activity
host with a `BottomNavigationView`), Room, Retrofit, WorkManager, Firebase
Auth, and Jetpack DataStore for preferences. UI follows the design system in
[`docs/design/README.md`](docs/design/README.md).

## Screens

| Screen | Purpose |
| --- | --- |
| Login / Register | Email+password auth via Firebase, with a create-account flow |
| Dashboard | Current conditions for the saved location, with a LIVE/CACHED freshness signal |
| Forecast | Hourly list, 7-day list, and a single-series graph (temperature/precip/humidity/wind) |
| Sync & offline | Connectivity status and the pending `SyncQueue`, with a manual retry |
| Settings | Theme (light/dark/system), units (metric/imperial), language (English/isiZulu/Afrikaans), notifications, logout |

## Building

```bash
./gradlew assembleDebug   # build the debug APK
./gradlew build           # full build: compile, unit tests, lint
./gradlew test            # unit tests only
```

Requires JDK 17. `compileSdk`/`targetSdk` 34, `minSdk` 26.

## Project structure

```
app/src/main/java/com/group24/atmospheric/
├── data/
│   ├── local/        # Room entities/DAO, DataStore, ConnectivityObserver
│   ├── remote/        # Retrofit service + DTOs for Open-Meteo
│   └── repository/    # WeatherRepository, AuthRepository, SyncRepository
├── domain/model/       # Plain domain models shared by UI and data layers
└── ui/
    ├── login/, register/   # Pre-auth Activities (no bottom nav)
    └── dashboard/, forecast/, sync/, settings/   # Fragments hosted by MainActivity
```

## Design handoff

[`docs/design/`](docs/design/) contains the interactive HTML prototype, a
static screens reference, and the full design spec (colour tokens, spacing,
type scale, and per-screen behaviour) this app's UI was built against. Open
`docs/design/README.md` first.

## Localisation

isiZulu (`values-zu`) and Afrikaans (`values-af`) string resources are
included but are machine-assisted, unverified translations — see the note at
the top of each file. They cover the app's core navigation and screen labels;
untranslated strings fall back to English by design. Have a native speaker
review them before treating them as final.

## CI

`.github/workflows/blank.yml` runs `./gradlew build` and `./gradlew test` on
every push to `main`.
