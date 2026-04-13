# Kalendar: Solaris

A swipeable full-month grid calendar. Each horizontal swipe moves one month forward or backward.
Arrow buttons are not shown; navigation is gesture-only. Supports single, multiple, and range
selection including cross-month ranges.

## Usage

```kotlin
Kalendar(
    type = KalendarType.Solaris,
    selectedDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    events = myEvents,
    onDaySelectionAction = OnDaySelectionAction.Single { date, events ->
        println("Selected $date with ${events.size} events")
    },
    config = KalendarConfig(
        startDayOfWeek = DayOfWeek.SUNDAY,
        minDate = LocalDate(2020, 1, 1),
        maxDate = LocalDate(2030, 12, 31),
        onVisibleRangeChange = { start, end ->
            loadEvents(start, end)
        },
    ),
)
```

### Cross-month range selection

```kotlin
Kalendar(
    type = KalendarType.Solaris,
    onDaySelectionAction = OnDaySelectionAction.Range { range, events ->
        println("Range: ${range.start} → ${range.endInclusive}")
    },
    config = KalendarConfig(
        initialSelectedRange = KalendarSelectedDayRange(
            start = LocalDate(2026, 4, 28),
            endInclusive = LocalDate(2026, 5, 5),
        ),
    ),
)
```

### Programmatic navigation

```kotlin
val controller = rememberKalendarController()

Kalendar(
    type = KalendarType.Solaris,
    controller = controller,
    ...
)

Button(onClick = { scope.launch { controller.scrollToDate(LocalDate(2026, 12, 1)) } }) {
    Text("Jump to December")
}
```

## Parameters

| Parameter | Type | Default | Description |
|---|---|---|---|
| `type` | `KalendarType` | — | Must be `KalendarType.Solaris`. |
| `modifier` | `Modifier` | `Modifier` | Applied to the outermost layout. |
| `selectedDate` | `LocalDate` | today | Initially highlighted date; determines the initially visible month page. |
| `events` | `KalendarEvents` | `emptyList()` | Events shown as indicator dots on day cells. |
| `onDaySelectionAction` | `OnDaySelectionAction` | `NoOp` | Single, Multiple, or Range selection handler. |
| `config` | `KalendarConfig` | `KalendarConfig()` | All visual and behavioural settings. `showArrows` has no effect on this variant. |
| `controller` | `KalendarController?` | `null` | Programmatic month navigation via `scrollToDate`. |
| `dayContent` | composable lambda? | `null` | Fully replaces the built-in day cell. Receives `date`, `isSelected`, and `events`. |

> **Note:** `config.showArrows` is ignored — Solaris is swipe-only by design.
> Out-of-month padding dates are rendered (dimmed) so that cross-month ranges display correctly.

See [Config.md](Config.md) for all `KalendarConfig` fields.
