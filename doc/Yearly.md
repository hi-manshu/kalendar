# Kalendar: Yearly

A year-overview calendar that shows all 12 months in a compact scrollable grid. Navigate between
years with previous/next arrow buttons. Tapping a day fires `onDaySelectionAction` and highlights
the selected date.

## Usage

```kotlin
Kalendar(
    type = KalendarType.Yearly,
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

## Parameters

| Parameter | Type | Default | Description |
|---|---|---|---|
| `type` | `KalendarType` | — | Must be `KalendarType.Yearly`. |
| `modifier` | `Modifier` | `Modifier` | Applied to the outermost layout. |
| `selectedDate` | `LocalDate` | today | Initially highlighted date. |
| `events` | `KalendarEvents` | `emptyList()` | Events shown as dots on mini day cells. |
| `onDaySelectionAction` | `OnDaySelectionAction` | `NoOp` | Single tap handling. |
| `config` | `KalendarConfig` | `KalendarConfig()` | All visual and behavioural settings. |
| `controller` | `KalendarController?` | `null` | Programmatic year navigation via `scrollToDate`. |
| `dayContent` | composable lambda? | `null` | Replaces the built-in mini day cell per month. |

See [Config.md](Config.md) for all `KalendarConfig` fields.
