# Changelog

All notable changes to Kalendar will be documented in this file. The format is
based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and this
project adheres to [Semantic Versioning](https://semver.org/).

---

## [Unreleased]

### Added
- **`KalendarType.Season`** — season-overview calendar showing the three meteorological months of
  the current season (Spring / Summer / Autumn / Winter) with season-tinted chrome. Winter spans
  the December–February year boundary. Includes public helpers `seasonPeriodOf`,
  `KalendarSeasonPeriod`, and `KalendarSeason.defaultPalette()`.
- **`LocalDate.isDateInteractable`** / **`isRangeFullyAllowed`** — shared selection policy for
  disabled dates, min/max bounds, and non-primary (pad) days.
- **`KalendarHeader.canNavigateForward`** — next-arrow enablement (mirrors `canNavigateBack`).

### Changed
- **`dayContent`** now receives a fourth argument `isDisabled: Boolean`. Hosts must honour it.
- **`onDayClick`** ignores disallowed dates and refuses range completion when any day in the
  range is disallowed.
- **`onVisibleRangeChange`** (Oceanic/Solaris) reports the full rendered grid including pad days.
- **`KalendarDay.selectedDate`** is nullable (`null` = no single-selection highlight).
- Agenda supports **Range** selection and respects `disabledDates` / min / max.
- Yearly mini-months require `date.year == year` (not only month).

### Fixed
- Next navigation arrow stayed enabled at `maxDate` (header always `enabled = true`).
- Custom `dayContent` paths no longer silently drop disable state (library now passes `isDisabled`).

### Sample / QA
- Sample type switcher (Oceanic / Firey / Season / Agenda).
- Oceanic demo: series days (blue) vs user selection (coral); past dates disabled.
- Compose UI tests (desktop): past-date disable, maxDate next arrow, Monday-first pad day.
- `KalendarDay` semantics: content description, testTag `kalendar-day-<date>`, selected/disabled.
- CI runs on `feat/**` / `fix/**` / `ci/**`; wrapper validation; APK skips pure docs.

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
