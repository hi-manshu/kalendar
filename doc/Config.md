# Kalendar – Configuration Reference

All visual and behavioural options are consolidated in `KalendarConfig`. Pass it to the `config`
parameter of `Kalendar()`.

---

## KalendarConfig

```kotlin
data class KalendarConfig(
    val showDayLabel: Boolean = true,
    val showArrows: Boolean = true,
    val startDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
    val firstVisibleDate: LocalDate? = null,
    val minDate: LocalDate? = null,
    val maxDate: LocalDate? = null,
    val initialSelectedDates: List<LocalDate> = emptyList(),
    val initialSelectedRange: KalendarSelectedDayRange? = null,
    val disabledDates: (LocalDate) -> Boolean = { false },
    val onVisibleRangeChange: ((start: LocalDate, end: LocalDate) -> Unit)? = null,
    val dayConfig: KalendarDayConfig = KalendarDayConfig(),
    val headerConfig: KalendarHeaderConfig = KalendarHeaderConfig.default(),
    val dayLabelConfig: KalendarDayLabelConfig = KalendarDayLabelConfig.default(),
    val backgroundColor: KalendarColor = KalendarColor.Solid(Color.White),
)
```

| Parameter | Type | Default | Description |
|---|---|---|---|
| `showDayLabel` | `Boolean` | `true` | Show the Mon/Tue/… header row. |
| `showArrows` | `Boolean` | `true` | Show previous/next arrow buttons (no effect on swipe variants). |
| `startDayOfWeek` | `DayOfWeek` | `MONDAY` | First column of the week grid. |
| `firstVisibleDate` | `LocalDate?` | `null` | Open the calendar on this date without pre-selecting it. |
| `minDate` | `LocalDate?` | `null` | Oldest navigable date. |
| `maxDate` | `LocalDate?` | `null` | Latest navigable date. |
| `initialSelectedDates` | `List<LocalDate>` | `emptyList()` | Pre-selected dates for Multiple mode. |
| `initialSelectedRange` | `KalendarSelectedDayRange?` | `null` | Pre-selected range for Range mode. |
| `disabledDates` | `(LocalDate) -> Boolean` | `{ false }` | Predicate; disabled dates are dimmed and non-tappable. |
| `onVisibleRangeChange` | `((LocalDate, LocalDate) -> Unit)?` | `null` | Fires on every navigation; use for lazy event loading. |
| `dayConfig` | `KalendarDayConfig` | — | Visual config for day cells. |
| `headerConfig` | `KalendarHeaderConfig` | — | Visual config for the month/week header. |
| `dayLabelConfig` | `KalendarDayLabelConfig` | — | Visual config for day-of-week labels. |
| `backgroundColor` | `KalendarColor` | `Solid(White)` | Background colour/gradient for the container. |

---

## KalendarDayConfig

```kotlin
data class KalendarDayConfig(
    val size: Dp = 56.dp,
    val selectedTextColor: KalendarColor = Color(0xFF413D4B).asSolidColor(),
    val borderColor: KalendarColor = Color(0xFFC39EA1).asSolidColor(),
    val indicatorColor: KalendarColor = Color(0xFFD8A29E).asSolidColor(),
    val textStyle: TextStyle = ...,
    val selectedBackgroundColor: KalendarColor = Color(0xFFF7CFD3).asSolidColor(),
)
```

| Parameter | Description |
|---|---|
| `size` | Diameter of each day circle cell. |
| `selectedTextColor` | Text colour when a day is selected. |
| `borderColor` | Border ring colour shown on today's date. |
| `indicatorColor` | Fallback dot colour for events (overridden by `KalendarEvent.eventColor`). |
| `textStyle` | Text style for the day number. |
| `selectedBackgroundColor` | Background fill when a day is selected. |

---

## KalendarHeaderConfig

```kotlin
data class KalendarHeaderConfig(
    val textStyle: TextStyle,
    val centerAligned: Boolean,
)
```

| Parameter | Description |
|---|---|
| `textStyle` | Text style for the month/week title. |
| `centerAligned` | `true` → title centred with arrows flanking; `false` → title leading. |

---

## KalendarDayLabelConfig

```kotlin
data class KalendarDayLabelConfig(
    val textStyle: TextStyle,
    val textCharCount: Int = 2,
    val centerAligned: Boolean = true,
    val dayNameFormatter: ((DayOfWeek) -> String)? = null,
)
```

| Parameter | Description |
|---|---|
| `textStyle` | Text style for day-of-week labels. |
| `textCharCount` | Characters taken from the day name when `dayNameFormatter` is `null` (e.g. `2` → "Mo"). |
| `centerAligned` | Centre-aligns labels within their column. |
| `dayNameFormatter` | Optional lambda for locale-aware labels; replaces `textCharCount` when non-null. |

---

## KalendarColor

```kotlin
sealed class KalendarColor {
    data class Solid(val color: Color) : KalendarColor()
    data class Gradient(val colors: List<Color>) : KalendarColor()
}

fun Color.asSolidColor(): KalendarColor.Solid
fun List<Color>.asGradientColor(): KalendarColor.Gradient
```

---

## KalendarEvent

```kotlin
interface KalendarEvent {
    val date: LocalDate
    val eventName: String
    val eventDescription: String?
    val startTime: LocalDateTime? get() = null
    val endTime: LocalDateTime? get() = null
    val eventColor: Color? get() = null
}
```

`BasicKalendarEvent` is a ready-to-use `data class` implementation. All three optional fields
default to `null` so existing implementations remain source-compatible.

| Property | Description |
|---|---|
| `date` | Date the event falls on. |
| `eventName` | Short display name. |
| `eventDescription` | Optional longer description. |
| `startTime` | Optional start date-time; events on the same day sort by this in Agenda view. |
| `endTime` | Optional end date-time; shown alongside `startTime` in Agenda view. |
| `eventColor` | Per-event indicator dot colour; falls back to `KalendarDayConfig.indicatorColor`. |

---

## OnDaySelectionAction

```kotlin
sealed class OnDaySelectionAction {
    data class Single(val onDayClick: (LocalDate, List<KalendarEvent>) -> Unit) : OnDaySelectionAction()
    data class Multiple(val onDayClick: (LocalDate, List<KalendarEvent>) -> Unit) : OnDaySelectionAction()
    data class Range(val onRangeSelected: (KalendarSelectedDayRange, List<KalendarEvent>) -> Unit) : OnDaySelectionAction()

    companion object {
        val NoOp: OnDaySelectionAction
    }
}
```

---

## KalendarSelectedDayRange

```kotlin
data class KalendarSelectedDayRange(
    override val start: LocalDate,
    override val endInclusive: LocalDate,
) : ClosedRange<LocalDate>
```

---

## KalendarController

```kotlin
class KalendarController {
    suspend fun scrollToDate(date: LocalDate)
}

@Composable
fun rememberKalendarController(): KalendarController
```

Obtain via `rememberKalendarController()` and pass to `Kalendar(controller = …)`.
