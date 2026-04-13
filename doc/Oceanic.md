# Kalendar: Oceanic

A full-month grid calendar navigated with previous/next arrow buttons. Displays one month at a time
in a 7-column week grid. Supports single, multiple, and range day selection.

## Usage

```kotlin
Kalendar(
    type = KalendarType.Oceanic,
    selectedDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    events = myEvents,
    onDaySelectionAction = OnDaySelectionAction.Single { date, events ->
        println("Selected $date with ${events.size} events")
    },
    config = KalendarConfig(
        startDayOfWeek = DayOfWeek.SUNDAY,
        minDate = LocalDate(2020, 1, 1),
        maxDate = LocalDate(2030, 12, 31),
        disabledDates = { date -> date.dayOfWeek == DayOfWeek.SUNDAY },
        onVisibleRangeChange = { start, end ->
            loadEvents(start, end)
        },
    ),
)
```

### Range selection

```kotlin
Kalendar(
    type = KalendarType.Oceanic,
    onDaySelectionAction = OnDaySelectionAction.Range { range, events ->
        println("Range: ${range.start} → ${range.endInclusive}")
    },
    config = KalendarConfig(
        initialSelectedRange = KalendarSelectedDayRange(
            start = LocalDate(2026, 4, 10),
            endInclusive = LocalDate(2026, 4, 20),
        ),
    ),
)
```

### Custom day cell

```kotlin
Kalendar(
    type = KalendarType.Oceanic,
    dayContent = { date, isSelected, events ->
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .background(if (isSelected) Color.Blue else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            Text(date.dayOfMonth.toString())
        }
    },
)
```

## Parameters

| Parameter | Type | Default | Description |
|---|---|---|---|
| `type` | `KalendarType` | — | Must be `KalendarType.Oceanic`. |
| `modifier` | `Modifier` | `Modifier` | Applied to the outermost layout. |
| `selectedDate` | `LocalDate` | today | Initially highlighted date. |
| `events` | `KalendarEvents` | `emptyList()` | Events shown as indicator dots on day cells. |
| `onDaySelectionAction` | `OnDaySelectionAction` | `NoOp` | Single, Multiple, or Range selection handler. |
| `config` | `KalendarConfig` | `KalendarConfig()` | All visual and behavioural settings. |
| `controller` | `KalendarController?` | `null` | Programmatic month navigation via `scrollToDate`. |
| `dayContent` | composable lambda? | `null` | Fully replaces the built-in day cell. Receives `date`, `isSelected`, and `events`. |

See [Config.md](Config.md) for all `KalendarConfig` fields.
