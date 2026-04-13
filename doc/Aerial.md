# Kalendar: Aerial

A swipeable week-strip calendar. Each horizontal swipe moves one week forward or backward.
Arrow buttons are not shown; navigation is gesture-only. Ideal for touch-first interfaces.

## Usage

```kotlin
Kalendar(
    type = KalendarType.Aerial,
    selectedDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    events = myEvents,
    onDaySelectionAction = OnDaySelectionAction.Single { date, events ->
        println("Selected $date")
    },
    config = KalendarConfig(
        startDayOfWeek = DayOfWeek.MONDAY,
        firstVisibleDate = LocalDate(2026, 4, 14),
        minDate = LocalDate(2020, 1, 1),
        maxDate = LocalDate(2030, 12, 31),
    ),
)
```

### Programmatic navigation

```kotlin
val controller = rememberKalendarController()

Kalendar(
    type = KalendarType.Aerial,
    controller = controller,
    ...
)

Button(onClick = { scope.launch { controller.scrollToDate(LocalDate(2026, 6, 1)) } }) {
    Text("Jump to June")
}
```

## Parameters

| Parameter | Type | Default | Description |
|---|---|---|---|
| `type` | `KalendarType` | — | Must be `KalendarType.Aerial`. |
| `modifier` | `Modifier` | `Modifier` | Applied to the outermost layout. |
| `selectedDate` | `LocalDate` | today | Initially highlighted date; determines the initially visible week page. |
| `events` | `KalendarEvents` | `emptyList()` | Events shown as indicator dots on day cells. |
| `onDaySelectionAction` | `OnDaySelectionAction` | `NoOp` | Single or Multiple selection handler. |
| `config` | `KalendarConfig` | `KalendarConfig()` | All visual and behavioural settings. `showArrows` has no effect on this variant. |
| `controller` | `KalendarController?` | `null` | Programmatic week navigation via `scrollToDate`. |
| `dayContent` | composable lambda? | `null` | Fully replaces the built-in day cell. Receives `date`, `isSelected`, and `events`. |

> **Note:** `config.showArrows` is ignored — Aerial is swipe-only by design.

See [Config.md](Config.md) for all `KalendarConfig` fields.
