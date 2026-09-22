# Atmospheric

Youtube link-
https://youtu.be/sg4wF9VYjgQ?si=LMsW-AlMqmetGsni

**OPSC6312 — Group 24**

An offline-first Android weather app: live forecasts when you have a
connection, a clearly-marked cached forecast when you don't.

[![Android CI](https://github.com/ST10443034/atmospheric/actions/workflows/blank.yml/badge.svg)](https://github.com/ST10443034/atmospheric/actions/workflows/blank.yml)

---

## Contents

- [Overview](#overview)
- [Download](#download)
- [Screens](#screens)
- [Tech stack](#tech-stack)
- [Building from source](#building-from-source)
- [Project structure](#project-structure)
- [Design handoff](#design-handoff)
- [Localisation](#localisation)
- [Team](#team)

## Overview

Atmospheric fetches current, hourly and daily forecasts from the
[Open-Meteo](https://open-meteo.com/) API and caches them in a local Room
database. When a live fetch fails, `WeatherRepository` falls back to the
last cached reading instead of showing an error, and the Dashboard makes
that distinction explicit with a **LIVE** / **CACHED** pill and a freshness
timestamp. A dedicated Sync & Offline screen surfaces connectivity state and
the pending sync queue, so "why is this stale?" always has a visible answer.

Users authenticate with Firebase (email/password, with account creation
in-app), and preferences — theme, units, language, notifications — persist
via Jetpack DataStore and apply live without a restart.

## Download

- **APK**: [`Atmospheric Gr24.apk`](Atmospheric%20Gr24.apk) — install
  directly on an Android device (enable "install from unknown sources" if
  prompted).
- **Walkthrough video**:[ [`APP TUTORIAL VIDEO.mp4`](APP%20TUTORIAL%20VIDEO.mp4)](https://youtu.be/sg4wF9VYjgQ?si=LMsW-AlMqmetGsni)

## Screens

| Screen | Purpose |
| --- | --- |
| Login / Register | Email+password auth via Firebase, with a create-account flow |
| Dashboard | Current conditions for the saved location, with a LIVE/CACHED freshness signal |
| Forecast | Hourly list, 7-day list, and a single-series graph (temperature/precip/humidity/wind) |
| Sync & offline | Connectivity status and the pending `SyncQueue`, with a manual retry |
| Settings | Theme (light/dark/system), units (metric/imperial), language (English/isiZulu/Afrikaans), notifications, logout |

## Tech stack

- **Language**: Kotlin
- **Architecture**: MVVM, single-Activity + Fragments, view binding
- **Navigation**: Jetpack Navigation with a `BottomNavigationView`
- **Persistence**: Room (offline cache + sync queue), Jetpack DataStore (preferences)
- **Networking**: Retrofit + Gson against the Open-Meteo API
- **Background work**: WorkManager
- **Auth**: Firebase Authentication
- **CI**: GitHub Actions (`./gradlew build` + `./gradlew test` on every push to `main`)

## Building from source

Requires JDK 17. `compileSdk` / `targetSdk` 34, `minSdk` 26.

```bash
./gradlew assembleDebug   # build the debug APK
./gradlew build           # full build: compile, unit tests, lint
./gradlew test            # unit tests only
```

The debug APK lands at `app/build/outputs/apk/debug/app-debug.apk`.

## Project structure

```
app/src/main/java/com/group24/atmospheric/
├── data/
│   ├── local/        # Room entities/DAO, DataStore, ConnectivityObserver
│   ├── remote/       # Retrofit service + DTOs for Open-Meteo
│   └── repository/   # WeatherRepository, AuthRepository, SyncRepository
├── domain/model/     # Plain domain models shared by UI and data layers
└── ui/
    ├── login/, register/                         # Pre-auth Activities (no bottom nav)
    └── dashboard/, forecast/, sync/, settings/    # Fragments hosted by MainActivity
```

## Design handoff

[`docs/design/`](docs/design/) contains the interactive HTML prototype, a
static screens reference, and the full design spec (colour tokens, spacing,
type scale, and per-screen behaviour) this app's UI was built against. Start
with [`docs/design/README.md`](docs/design/README.md).

## Localisation

isiZulu (`values-zu`) and Afrikaans (`values-af`) string resources are
included but are machine-assisted, **unverified** translations — see the
note at the top of each file. They cover the app's core navigation and
screen labels; untranslated strings fall back to English by design. Have a
native speaker review them before treating them as final.

## Team

| Student number | Name |
| --- | --- |
| ST10443034 | Oluga Jeffrey Neluvhalani |
| ST10446457 | Luthando Princess Mndawe |


<!-- Add remaining Group 24 members here. -->
