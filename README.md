# Atmospheric

**OPSC6312 — Group 24 · Part 3 (final PoE)**

An offline-first Android weather app: live forecasts when you have a
connection, a clearly-marked cached forecast when you don't. *Your weather,
even when you're offline.*

Repository: https://github.com/ST10443034/atmospheric

[![Android CI](https://github.com/ST10443034/atmospheric/actions/workflows/blank.yml/badge.svg)](https://github.com/ST10443034/atmospheric/actions/workflows/blank.yml)

---

## Contents

- [Demo video](#demo-video)
- [Screenshots](#screenshots)
- [Purpose of the app](#purpose-of-the-app)
- [Feature checklist (Part 3 brief)](#feature-checklist-part-3-brief)
- [Design considerations](#design-considerations)
- [Architecture and data](#architecture-and-data)
- [Using GitHub and GitHub Actions](#using-github-and-github-actions)
- [Release notes](#release-notes)
- [Publishing preparation](#publishing-preparation)
- [Building from source](#building-from-source)
- [Firebase setup](#firebase-setup)
- [Localisation](#localisation)
- [AI tool use](#ai-tool-use)
- [Team](#team)

## Demo video

- **Final PoE walkthrough (v2.0, physical phone, voice-over):** _add the unlisted YouTube link here before submitting_
- Part 2 prototype walkthrough: https://youtu.be/sg4wF9VYjgQ (also `APP TUTORIAL VIDEO.mp4` in this repo)

The final video shows register/login, Google SSO, settings, the REST API,
offline mode with sync, push notifications, multi-language, and the data
stored in Firebase.

## Screenshots

| Login | Dashboard | Forecast |
| --- | --- | --- |
| ![Login](docs/screenshots/login.png) | ![Dashboard](docs/screenshots/dashboard.png) | ![Forecast](docs/screenshots/forecast.png) |

| Sync & Offline | Settings | Notification |
| --- | --- | --- |
| ![Sync](docs/screenshots/sync.png) | ![Settings](docs/screenshots/settings.png) | ![Notification](docs/screenshots/notification.png) |

## Purpose of the app

Weather apps fail exactly when connectivity is worst. Atmospheric fetches
current, hourly and daily forecasts from the open
[Open-Meteo](https://open-meteo.com/) REST API and caches them in a local
Room database. When a live fetch fails the app shows the last cached reading
and says so, using a **LIVE** / **CACHED** pill and a freshness timestamp. A
dedicated Sync & Offline screen shows connectivity and the pending sync
queue, so "why is this stale?" always has a visible answer.

## Feature checklist (Part 3 brief)

| Requirement | How it is met |
| --- | --- |
| Register and log in, encrypted password | Firebase Authentication email/password. Passwords are salted and hashed by Firebase and never stored in the app or repo. Inline validation for bad input |
| Single sign-on (SSO) | "Continue with Google" via Google Sign-In → Firebase Authentication |
| Change settings | Theme, units (metric/imperial), language, notifications, profile; persisted with Jetpack DataStore and synced to Firestore |
| REST API | Retrofit + Gson against the public Open-Meteo forecast API |
| Offline mode with sync | Room cache + `SyncQueue`; cached data is flagged and queued work retries when the connection returns. Firestore also queues offline profile/settings writes and uploads them on reconnect |
| Real-time notifications | Firebase Cloud Messaging (topic `weather_alerts`) for pushed alerts, plus an hourly WorkManager job that raises local severe-weather alerts (thunderstorm, extreme heat, heavy rain, strong wind) |
| Multi-language (2+ South African languages) | English, isiZulu and Afrikaans, switchable in-app without changing the device language |
| NoSQL database (store and read) | Cloud Firestore document `users/{uid}`: display name, preferences, FCM token; read back on the Settings screen |
| Blob storage | Profile photo is resized to 256 px, JPEG-compressed and stored as Base64 in Firestore (kept in Firestore deliberately so no paid Cloud Storage plan is needed) |
| App icon and final image assets | Adaptive launcher icon (cloud and sun) in `res/mipmap-*` and `res/drawable` |
| Part 1 features | All five planned screens, bottom navigation, Room, DataStore, WorkManager, FCM |

## Design considerations

- **Offline first.** The UI never talks to the network directly. Data flows
  `Open-Meteo → Retrofit → WeatherRepository → Room → ViewModel → UI`, and
  the repository falls back to Room on failure.
- **Honest freshness.** Cached data is always labelled; the forecast graph
  changes colour when it is showing cached data.
- **Units are a display concern.** Everything is stored in metric and
  converted only when shown (`UnitSystem`), so the cache never mixes unit
  systems and switching units updates every screen instantly.
- **Opt-in notifications.** Alerts are off until the user turns the switch on
  and (on Android 13+) grants the permission. Turning it off unsubscribes the
  device from the push topic and stops local alerts.
- **Invalid input never crashes the app.** Email/password checks give inline
  messages; failed network, Google or Firebase calls surface as errors.
- **Small dependency surface.** The forecast graph is a custom Canvas view.
- **Design spec.** The UI follows the handoff in [`docs/design/`](docs/design/README.md).

## Architecture and data

MVVM, single-Activity with Fragments, view binding, Jetpack Navigation.

```
app/src/main/java/com/group24/atmospheric/
├── data/
│   ├── local/        # Room entities/DAO, DataStore, ConnectivityObserver
│   ├── remote/       # Retrofit service + DTOs for Open-Meteo
│   └── repository/   # Weather, Auth (email + Google), Sync, UserProfile (Firestore)
├── domain/model/     # Weather models, UnitSystem, AlertRules
├── notifications/    # Channel + notifications, FCM service, topic opt-in
├── work/             # WorkManager hourly sync + alert worker, scheduler
├── util/             # ImageCodec (photo resize / Base64)
└── ui/               # login, register, dashboard, forecast, sync, settings
```

What is stored online (visible in the Firebase console during the demo):

- **Authentication:** user accounts (email, provider, UID); passwords hashed.
- **Firestore** `users/{uid}`: `displayName`, `photoBase64`, `units`,
  `language`, `theme`, `notificationsEnabled`, `fcmToken`, `email`, `updatedAt`.
- **Open-Meteo:** read-only public API (no account, nothing stored about the user).

## Using GitHub and GitHub Actions

- **Version control:** all source is committed to this repository (no zip
  files). Work is committed in small steps; the final submission commit is
  tagged `final-poe`.
- **CI** ([`blank.yml`](.github/workflows/blank.yml)): on every push and pull
  request to `main` (and on tags) it sets up JDK 17, runs
  `./gradlew build` (compile, Android Lint, unit tests), `./gradlew test`, and
  assembles the debug APK. Test/lint reports and the debug APK are uploaded as
  build artifacts. CI caught a real problem earlier in development (Lint's
  `MissingTranslation` check failing on partial translations).
- **Release** ([`release.yml`](.github/workflows/release.yml)): pushing a `v*`
  tag builds a signed release APK using keystore details held in GitHub
  Actions secrets, never in the repository.
- **Automated tests** (`app/src/test`): unit conversion (`UnitSystemTest`),
  alert rules (`AlertRulesTest`) and photo sizing (`ImageCodecTest`). The
  placeholder example test from the prototype was removed.

## Release notes

### v2.0 (final PoE) — changes since the prototype (v1.0)

**Innovative features added**

- **Severe-weather alerts that really work.** The notification switch is now
  wired to behaviour: it asks for the Android 13+ permission, subscribes the
  device to FCM push alerts, and enables hourly background checks that raise
  local alerts for thunderstorms, extreme heat, heavy rain and strong wind.
  Alert text follows the chosen language and units.
- **Background sync.** WorkManager refreshes the Room cache every hour when
  online, with exponential-backoff retries.
- **Cloud profile and synced settings.** Cloud Firestore stores the user's
  name, preferences and a profile photo (compressed Base64 blob), readable and
  editable from Settings, with Firestore's offline write queue.
- **Single sign-on.** Google Sign-In through Firebase Authentication.

**Fixes from lecturer feedback on the prototype**

- Units now apply everywhere: temperature (°C/°F) and wind (km/h/mph) on the
  Dashboard, hourly strip, daily list and graph. Previously the units were
  stored but the screens were hard-coded to °C and km/h.
- Notification switch is connected to real behaviour (see above).
- Replaced the default `2 + 2 = 4` example test with real unit tests.
- Temperatures are rounded rather than truncated; the graph header shows the
  correct unit for each variable (°, %, km/h or mph).

**Other changes**

- New strings translated for isiZulu and Afrikaans (machine-assisted, unverified).
- Logging added around authentication, sync, notifications and profile saving.
- CI now caches Gradle and uploads reports and the APK; added a signed-release workflow.
- Version 2.0 (versionCode 2).

**Known limitations**

- Location is a fixed default (Johannesburg); a location picker is future work.
- The Room cache stores only the current reading, so hourly/daily lists are
  empty while offline until a live fetch has populated them.
- Translations need native-speaker review.

## Publishing preparation

- Application ID `com.group24.atmospheric`, versionName 2.0 / versionCode 2,
  minSdk 26, targetSdk 34, adaptive launcher icon.
- Signed release APK built with a private keystore (kept out of Git; see
  `.gitignore`). Evidence (signed APK, store screenshots, Play Console
  screenshot if applicable) is included with the submission.
- Release build: `./gradlew assembleRelease` with `keystore.properties` or the
  `KEYSTORE_*` environment variables present.

## Building from source

Requires JDK 17. `compileSdk` / `targetSdk` 34, `minSdk` 26.

```bash
./gradlew assembleDebug     # build the debug APK
./gradlew build             # compile, unit tests, lint
./gradlew test              # unit tests only
./gradlew assembleRelease   # signed release APK (needs keystore.properties)
```

The debug APK lands at `app/build/outputs/apk/debug/app-debug.apk`.

## Firebase setup

`app/google-services.json` connects the app to the Firebase project. To run
your own copy: create a Firebase project, add the Android app
`com.group24.atmospheric` with your SHA-1, enable **Authentication**
(Email/Password and Google) and **Cloud Firestore**, then replace
`google-services.json`. Cloud Storage is intentionally not used.

## Localisation

isiZulu (`values-zu`) and Afrikaans (`values-af`) string resources cover the
app's navigation, screen labels and the new alert text. They are
machine-assisted, **unverified** translations; untranslated strings fall back
to English. Have a native speaker review them before treating them as final.

## AI tool use

See [`docs/AI_USAGE.md`](docs/AI_USAGE.md) for the short write-up of how AI tools were used and cited.

## Team

| Student number | Name |
| --- | --- |
| ST10443034 | Oluga Jeffrey Neluvhalani |
| ST10446457 | Luthando Mndawe |
