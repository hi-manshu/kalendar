# Kalendar: Agenda

An agenda-style calendar that renders a scrollable list of events grouped by date. Events within the
same day are sorted by `KalendarEvent.startTime` when available. An empty-state message is displayed
when no events are provided.

## Usage

```kotlin
val events = listOf(
    BasicKalendarEvent(
        date = LocalDate(2026, 4, 14),
        eventName = "Team standup",
        startTime = LocalDateTime(LocalDate(2026, 4, 14), LocalTime(9, 0)),
        endTime = LocalDateTime(LocalDate(2026, 4, 14), LocalTime(9, 30)),
        eventColor = Color(0xFF4CAF50),
    ),
    BasicKalendarEvent(
        date = LocalDate(2026, 4, 14),
        eventName = "Design review",
        startTime = LocalDateTime(LocalDate(2026, 4, 14), LocalTime(14, 0)),
    ),
)

Kalendar(
    type = KalendarType.Agenda,
    events = events,
    config = KalendarConfig(
        backgroundColor = KalendarColor.Solid(Color.White),
    ),
)
```

## Parameters

| Parameter | Type | Default | Description |
|---|---|---|---|
| `type` | `KalendarType` | — | Must be `KalendarType.Agenda`. |
| `modifier` | `Modifier` | `Modifier` | Applied to the outermost layout. |
| `events` | `KalendarEvents` | `emptyList()` | Events to list. Sorted by date, then by `startTime` within each day. |
| `onDaySelectionAction` | `OnDaySelectionAction` | `NoOp` | Not used; reserved for future support. |
| `config` | `KalendarConfig` | `KalendarConfig()` | Visual settings (`backgroundColor`, `headerConfig`). |

> **Note:** `selectedDate`, `controller`, and `dayContent` have no effect on `KalendarType.Agenda`.

See [Config.md](Config.md) for all `KalendarConfig` fields.
