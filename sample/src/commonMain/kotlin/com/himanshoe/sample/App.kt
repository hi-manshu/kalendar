/*
 *
 *  * Copyright 2026 Kalendar Contributors (https://www.himanshoe.com). All rights reserved.
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *
 */

package com.himanshoe.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.himanshoe.kalendar.Kalendar
import com.himanshoe.kalendar.KalendarType
import com.himanshoe.kalendar.foundation.action.OnDaySelectionAction
import com.himanshoe.kalendar.foundation.color.asSolidColor
import com.himanshoe.kalendar.foundation.component.KalendarDay
import com.himanshoe.kalendar.foundation.component.config.KalendarConfig
import com.himanshoe.kalendar.foundation.component.config.KalendarDayConfig
import com.himanshoe.kalendar.foundation.component.config.KalendarDayLabelConfig
import com.himanshoe.kalendar.foundation.event.BasicKalendarEvent
import com.himanshoe.kalendar.foundation.event.KalendarEvents
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

private val SeriesBlue = Color(0xFF5B8DEF)
private val SelectionCoral = Color(0xFFE86A5D)
private val ChipSelected = Color(0xFF413D4B)
private val ChipIdle = Color(0xFF9E9E9E)

/**
 * Demo types exposed in the sample type switcher for QA matrix coverage.
 */
enum class SampleKalendarDemo {
    Oceanic,
    Firey,
    Season,
    Agenda,
}

/**
 * Sample entry: type switcher + Monday-first demos with past dates disabled.
 */
@Composable
fun App() {
    var demo by remember { mutableStateOf(SampleKalendarDemo.Oceanic) }
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    Column(
        modifier = Modifier
            .wrapContentSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Kalendar sample",
            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
        )
        DemoTypeSwitcher(selected = demo, onSelect = { demo = it })
        Text(
            text = demoCaption(demo),
            style = TextStyle(fontSize = 13.sp, color = Color(0xFF616161)),
        )
        when (demo) {
            SampleKalendarDemo.Oceanic -> MondayMonthRecurringWeekKalendar(
                recurringWeekDays = setOf(DayOfWeek.WEDNESDAY),
                eventName = "Team standup",
                seriesColor = SeriesBlue,
                selectionColor = SelectionCoral,
            )
            SampleKalendarDemo.Firey -> SimpleTypedKalendar(
                type = KalendarType.Firey,
                today = today,
            )
            SampleKalendarDemo.Season -> SimpleTypedKalendar(
                type = KalendarType.Season,
                today = today,
            )
            SampleKalendarDemo.Agenda -> AgendaDemo(today = today)
        }
    }
}

@Composable
private fun DemoTypeSwitcher(
    selected: SampleKalendarDemo,
    onSelect: (SampleKalendarDemo) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SampleKalendarDemo.entries.forEach { demo ->
            val active = demo == selected
            Button(
                onClick = { onSelect(demo) },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (active) ChipSelected else ChipIdle,
                    contentColor = Color.White,
                ),
            ) {
                Text(demo.name)
            }
        }
    }
}

private fun demoCaption(demo: SampleKalendarDemo): String = when (demo) {
    SampleKalendarDemo.Oceanic ->
        "Month · Mon-first · Wed series (blue) · selection (coral) · past disabled"
    SampleKalendarDemo.Firey -> "Week row · past disabled · minDate = today"
    SampleKalendarDemo.Season -> "Season overview · past disabled"
    SampleKalendarDemo.Agenda -> "Agenda list · fixture events · past disabled"
}

/**
 * Oceanic month calendar with weekly series highlight distinct from user selection.
 *
 * Series days use [seriesColor] via multi-select fill. The tapped day uses [selectionColor].
 * Dates before today and non-primary month pad cells are disabled by library policy.
 */
@Composable
fun MondayMonthRecurringWeekKalendar(
    recurringWeekDays: Set<DayOfWeek>,
    eventName: String,
    seriesColor: Color,
    selectionColor: Color,
    modifier: Modifier = Modifier,
    seriesYear: Int = Clock.System.todayIn(TimeZone.currentSystemDefault()).year,
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    var selectedDate by remember { mutableStateOf(today) }
    val weekStart = DayOfWeek.MONDAY

    val expansionRange = remember(seriesYear, weekStart) {
        seriesExpansionRangeForYear(seriesYear, weekStart)
    }
    val recurringDates = remember(recurringWeekDays, expansionRange) {
        expandWeeklyRecurrence(
            weekDays = recurringWeekDays,
            rangeStart = expansionRange.start,
            rangeEnd = expansionRange.endInclusive,
        )
    }
    val recurringDateSet = remember(recurringDates) { recurringDates.toSet() }

    val events: KalendarEvents = remember(recurringDates, eventName, seriesColor) {
        recurringDates.map { date ->
            BasicKalendarEvent(
                date = date,
                eventName = eventName,
                eventDescription = "Weekly recurring",
                eventColor = seriesColor,
            )
        }
    }

    val baseText = TextStyle(
        fontSize = 16.sp,
        brush = Brush.linearGradient(listOf(Color(0xFF413D4B), Color(0xFF413D4B))),
    )
    val seriesDayConfig = remember(seriesColor) {
        KalendarDayConfig(
            selectedBackgroundColor = seriesColor.asSolidColor(),
            selectedTextColor = Color.White.asSolidColor(),
            indicatorColor = seriesColor.asSolidColor(),
            borderColor = seriesColor.asSolidColor(),
            textStyle = baseText,
        )
    }
    val selectionDayConfig = remember(selectionColor) {
        KalendarDayConfig(
            selectedBackgroundColor = selectionColor.asSolidColor(),
            selectedTextColor = Color.White.asSolidColor(),
            indicatorColor = selectionColor.asSolidColor(),
            borderColor = selectionColor.asSolidColor(),
            textStyle = baseText,
        )
    }
    val idleDayConfig = remember {
        KalendarDayConfig(textStyle = baseText)
    }

    Kalendar(
        type = KalendarType.Oceanic,
        selectedDate = selectedDate,
        modifier = modifier.fillMaxWidth(),
        events = events,
        onDaySelectionAction = OnDaySelectionAction.NoOp,
        config = KalendarConfig(
            startDayOfWeek = weekStart,
            dayConfig = idleDayConfig,
            dayLabelConfig = KalendarDayLabelConfig.default().copy(
                dayNameFormatter = { day -> mondayFirstShortLabel(day) },
            ),
            minDate = today,
            disabledDates = { it < today },
        ),
        dayContent = { date, isSelected, dayEvents, isDisabled ->
            val isRecurring = date in recurringDateSet && !isDisabled
            val dayConfig = when {
                isSelected -> selectionDayConfig
                isRecurring -> seriesDayConfig
                else -> idleDayConfig
            }
            KalendarDay(
                date = date,
                selectedDate = if (isSelected) date else null,
                selectedDates = if (isRecurring && !isSelected) listOf(date) else emptyList(),
                events = dayEvents,
                dayConfig = dayConfig,
                isDisabled = isDisabled,
                onDayClick = { clicked, _ ->
                    if (!isDisabled) {
                        selectedDate = clicked
                    }
                },
            )
        },
    )
}

@Composable
private fun SimpleTypedKalendar(
    type: KalendarType,
    today: LocalDate,
    modifier: Modifier = Modifier,
) {
    var selectedDate by remember { mutableStateOf(today) }
    val events = remember(today) {
        listOf(
            BasicKalendarEvent(date = today, eventName = "Today", eventColor = SeriesBlue),
            BasicKalendarEvent(
                date = today.plus(1, DateTimeUnit.DAY),
                eventName = "Tomorrow",
                eventColor = SeriesBlue,
            ),
        )
    }

    Kalendar(
        type = type,
        selectedDate = selectedDate,
        modifier = modifier.fillMaxWidth(),
        events = events,
        onDaySelectionAction = OnDaySelectionAction.Single { date, _ ->
            selectedDate = date
        },
        config = KalendarConfig(
            startDayOfWeek = DayOfWeek.MONDAY,
            minDate = today,
            disabledDates = { it < today },
            dayConfig = KalendarDayConfig(
                selectedBackgroundColor = SelectionCoral.asSolidColor(),
                selectedTextColor = Color.White.asSolidColor(),
            ),
        ),
    )
}

@Composable
private fun AgendaDemo(
    today: LocalDate,
    modifier: Modifier = Modifier,
) {
    val events = remember(today) {
        listOf(
            BasicKalendarEvent(
                date = today.minus(2, DateTimeUnit.DAY),
                eventName = "Past (disabled)",
                eventColor = Color.Gray,
            ),
            BasicKalendarEvent(date = today, eventName = "Standup", eventColor = SeriesBlue),
            BasicKalendarEvent(
                date = today.plus(3, DateTimeUnit.DAY),
                eventName = "Retro",
                eventColor = SelectionCoral,
            ),
        )
    }
    Kalendar(
        type = KalendarType.Agenda,
        modifier = modifier.fillMaxWidth(),
        events = events,
        onDaySelectionAction = OnDaySelectionAction.Single { _, _ -> },
        config = KalendarConfig(
            minDate = today,
            disabledDates = { it < today },
        ),
    )
}

/**
 * Inclusive date range for expanding a weekly series across every Monday-first month grid
 * cell in [year], including December pad of the previous year and January pad of the next.
 */
internal fun seriesExpansionRangeForYear(
    year: Int,
    startDayOfWeek: DayOfWeek,
): ClosedRange<LocalDate> {
    val januaryFirst = LocalDate(year, 1, 1)
    val decemberLast = LocalDate(year, 12, 31)
    val gridStart = startOfWeekContaining(januaryFirst, startDayOfWeek)
    val gridEnd = startOfWeekContaining(decemberLast, startDayOfWeek)
        .plus(6, DateTimeUnit.DAY)
    return gridStart..gridEnd
}

/**
 * First day of the week containing [date] for [startDayOfWeek].
 */
internal fun startOfWeekContaining(
    date: LocalDate,
    startDayOfWeek: DayOfWeek,
): LocalDate {
    val daysBack = (date.dayOfWeek.ordinal - startDayOfWeek.ordinal + 7) % 7
    return date.minus(daysBack, DateTimeUnit.DAY)
}

/**
 * Every [LocalDate] in [[rangeStart], [rangeEnd]] whose weekday is in [weekDays].
 */
internal fun expandWeeklyRecurrence(
    weekDays: Set<DayOfWeek>,
    rangeStart: LocalDate,
    rangeEnd: LocalDate,
): List<LocalDate> {
    if (weekDays.isEmpty() || rangeStart > rangeEnd) return emptyList()

    val dates = mutableListOf<LocalDate>()
    var cursor = rangeStart
    while (cursor <= rangeEnd) {
        if (cursor.dayOfWeek in weekDays) {
            dates += cursor
        }
        cursor = cursor.plus(1, DateTimeUnit.DAY)
    }
    return dates
}

/**
 * Short weekday labels for a Monday-first grid.
 */
internal fun mondayFirstShortLabel(day: DayOfWeek): String = when (day) {
    DayOfWeek.MONDAY -> "Mon"
    DayOfWeek.TUESDAY -> "Tue"
    DayOfWeek.WEDNESDAY -> "Wed"
    DayOfWeek.THURSDAY -> "Thu"
    DayOfWeek.FRIDAY -> "Fri"
    DayOfWeek.SATURDAY -> "Sat"
    DayOfWeek.SUNDAY -> "Sun"
}
