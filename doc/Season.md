# Kalendar: Season

A season-overview calendar that shows the three meteorological months of the current season
(Spring / Summer / Autumn / Winter) in a compact scrollable layout. Navigate between seasons with
previous/next arrow buttons. Chrome tints adapt to the active season. Winter spans the
December–February year boundary.

## Seasons

| Season | Months |
|---|---|
| Spring | March – May |
| Summer | June – August |
| Autumn | September – November |
| Winter | December – February (cross-year) |

## Usage

```kotlin
Kalendar(
    type = KalendarType.Season,
    selectedDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    events = myEvents,
    onDaySelectionAction = OnDaySelectionAction.Single { date, events ->
        println("Selected $date")
    },
    config = KalendarConfig(
        minDate = LocalDate(2020, 1, 1),
        maxDate = LocalDate(2030, 12, 31),
    ),
)
```

### Custom day cell

```kotlin
Kalendar(
    type = KalendarType.Season,
    dayContent = { date, isSelected, events ->
        Text(
            text = date.dayOfMonth.toString(),
            color = if (isSelected) Color.Blue else Color.Unspecified,
        )
    },
)
```

### Season helpers

```kotlin
val period = seasonPeriodOf(LocalDate(2026, 12, 15))
// KalendarSeasonPeriod(year = 2026, season = Winter)
// title: "Winter 2026–2027"
// months: December 2026, January 2027, February 2027

val palette = period.season.defaultPalette()
```

## Parameters

| Parameter | Type | Default | Description |
|---|---|---|---|
| `type` | `KalendarType` | — | Must be `KalendarType.Season`. |
| `modifier` | `Modifier` | `Modifier` | Applied to the outermost layout. |
| `selectedDate` | `LocalDate` | today | Initially highlighted date; determines initially visible season. |
| `events` | `KalendarEvents` | `emptyList()` | Events shown as dots on mini day cells. |
| `onDaySelectionAction` | `OnDaySelectionAction` | `NoOp` | Single, Multiple, or Range selection handler. |
| `config` | `KalendarConfig` | `KalendarConfig()` | All visual and behavioural settings. |
| `controller` | `KalendarController?` | `null` | Programmatic season navigation via `scrollToDate`. |
| `dayContent` | composable lambda? | `null` | Replaces the built-in mini day cell per month. |

See [Config.md](Config.md) for all `KalendarConfig` fields.
