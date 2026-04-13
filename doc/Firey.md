# Kalendar: Firey

A week-strip calendar that displays a single scrollable week row. Navigate between weeks with
previous/next arrow buttons. Ideal for compact horizontal layouts.

## Usage

```kotlin
Kalendar(
    type = KalendarType.Firey,
    selectedDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    events = myEvents,
    onDaySelectionAction = OnDaySelectionAction.Single { date, events ->
        println("Selected $date")
    },
    config = KalendarConfig(
        startDayOfWeek = DayOfWeek.MONDAY,
        firstVisibleDate = LocalDate(2026, 4, 14),
        disabledDates = { date -> date < LocalDate(2026, 1, 1) },
    ),
)
```

### Multiple selection

```kotlin
Kalendar(
    type = KalendarType.Firey,
    onDaySelectionAction = OnDaySelectionAction.Multiple { date, events ->
        selectedDates += date
    },
    config = KalendarConfig(
        initialSelectedDates = listOf(
            LocalDate(2026, 4, 14),
            LocalDate(2026, 4, 16),
        ),
    ),
)
```

### Custom day cell

```kotlin
Kalendar(
    type = KalendarType.Firey,
    dayContent = { date, isSelected, events ->
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(date.dayOfWeek.name.take(1))
            Text(
                text = date.dayOfMonth.toString(),
                color = if (isSelected) Color.Red else Color.Unspecified,
            )
        }
    },
)
```

## Parameters

| Parameter | Type | Default | Description |
|---|---|---|---|
| `type` | `KalendarType` | — | Must be `KalendarType.Firey`. |
| `modifier` | `Modifier` | `Modifier` | Applied to the outermost layout. |
| `selectedDate` | `LocalDate` | today | Initially highlighted date; determines the initially visible week. |
| `events` | `KalendarEvents` | `emptyList()` | Events shown as indicator dots on day cells. |
| `onDaySelectionAction` | `OnDaySelectionAction` | `NoOp` | Single or Multiple selection handler. |
| `config` | `KalendarConfig` | `KalendarConfig()` | All visual and behavioural settings. |
| `controller` | `KalendarController?` | `null` | Programmatic week navigation via `scrollToDate`. |
| `dayContent` | composable lambda? | `null` | Fully replaces the built-in day cell. Receives `date`, `isSelected`, and `events`. |

> **Note:** Range selection via `OnDaySelectionAction.Range` is not visually supported in Firey.
> Use `KalendarType.Oceanic` or `KalendarType.Solaris` for range selection.

See [Config.md](Config.md) for all `KalendarConfig` fields.
