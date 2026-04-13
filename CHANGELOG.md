# Changelog

All notable changes to Kalendar will be documented in this file. The format is
based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and this
project adheres to [Semantic Versioning](https://semver.org/).

---

## [Unreleased]

## [2.0.0-RC1] – 2026

### Breaking changes from 1.x
- The library has been fully migrated to **Kotlin Multiplatform** (Android, JVM/Desktop, iOS, wasmJS).
- `kalendar` now depends on the new `kalendar-foundation` module which must be added separately if
  you need direct access to foundation types.
- `KalendarType` is now a **sealed interface** instead of an enum. Replace `KalendarType.OCEANIC`
  style usages with `KalendarType.Oceanic` data-object references.
- Day-click callbacks now receive `List<KalendarEvent>` (events on that day) in addition to the
  tapped `LocalDate`.
- Selection behaviour is now configured through `OnDaySelectionAction` instead of individual
  boolean flags.

### Added
- **Four calendar variants** via `KalendarType`:
  - `Oceanic` — full month grid with previous/next arrow navigation.
  - `Firey` — single week row with previous/next arrow navigation.
  - `Solaris` — swipeable full month grid (infinite `HorizontalPager`).
  - `Aerial` — swipeable week row (infinite `HorizontalPager`).
- **`OnDaySelectionAction`** sealed class — `Single`, `Multiple`, and `Range` selection modes.
- **`KalendarSelectedDayRange`** — typed value for range selection with compile-time start/end fields.
- **`KalendarEvents` / `KalendarEvent` / `BasicKalendarEvent`** — typed event model with per-day
  event indicators (up to 3 dots).
- **`KalendarKonfig` / `KalendarDayKonfig` / `KalendarHeaderKonfig` / `KalendarDayLabelKonfig`**
  — composable configuration objects for colours, sizes, and text styles.
- **`KalendarColor`** — sealed class for `Solid` and `Gradient` colour values used throughout the
  configuration API.
- **`startDayOfWeek`** parameter — configure the first column of the week grid.
- **`restrictToCurrentWeekOrMonth`** flag — prevent back-navigation past the current period.
- wasmJS target support.

### Fixed
- `isCurrentMonth` navigation guard now correctly handles cross-year navigation
  (e.g. viewing January 2026 from December 2026 no longer disables back navigation).

---

## [1.0.0]

Initial release supporting Android-only Jetpack Compose with basic month and week calendar views.
