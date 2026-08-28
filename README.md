# ADAM — GPS Fitness Tracker (Android)

Original GPS running/walking/cycling tracker. Kotlin + Jetpack Compose + Material 3,
Room (local DB only, no account/cloud), a foreground Service for background GPS
tracking, osmdroid (OpenStreetMap, no API key) for the live route map, native
Android Text-to-Speech for voice cues, and an Android Share Intent for the
Instagram-style share card.

## Why this is a source project, not a prebuilt .apk

This was generated in a sandboxed environment with no Android SDK and no network
access to Google's Maven repository or the Gradle distribution server — so an
APK could not be compiled here. Everything else is real, complete code: no
TODOs, no stub screens, no fake GPS/DB. Build it yourself in a few minutes:

## Build instructions

1. Install **Android Studio** (Koala or newer) — it bundles the Android SDK.
2. `File → Open` and select the unzipped `Adam` folder. Let Gradle sync
   (first sync downloads dependencies from Google/Maven Central — needs internet).
3. Plug in a device (or start an emulator) with **Android 8.0 (API 26)+**.
4. `Run ▶` to install and launch, **or** `Build → Build App Bundle(s)/APK(s) →
   Build APK(s)` to produce `app/build/outputs/apk/debug/app-debug.apk`.
5. For a signed release build: `Build → Generate Signed App Bundle / APK`.

No API keys, accounts, or backend setup needed — the app is fully offline-first
except for map tile downloads (GPS recording works with no signal/data at all).

## Project structure

- `data/` — Room entities/DAO/DB, DataStore-backed `SettingsRepository`
- `location/` — `LocationTrackingService` (foreground GPS service), tracking
  state models shared with the UI via a `StateFlow`
- `util/` — distance/pace/calorie math with GPS-jump filtering, GPX export,
  the share-card `Canvas` renderer, TTS announcer, route JSON codec
- `ui/` — one package per screen (home, activitystart, activeworkout, summary,
  history, detail, stats, settings) plus shared components and the nav host

## Notes / known follow-ups

- `bestPaceSecPerKm` is currently approximated as the workout's average pace;
  a true best-1km-split calculation is a good next iteration.
- App icon is a simple vector placeholder — swap `ic_launcher_foreground.xml`
  / `ic_launcher_background.xml` for real branded artwork.
- Tests folders aren't populated; the checklist items around GPS/permission
  edge cases are handled in code (see `LocationTrackingService`,
  `ActiveWorkoutScreen`) but don't yet have automated instrumented tests.
