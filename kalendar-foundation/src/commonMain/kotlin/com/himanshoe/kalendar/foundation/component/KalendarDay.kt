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

package com.himanshoe.kalendar.foundation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.kalendar.foundation.action.KalendarSelectedDayRange
import com.himanshoe.kalendar.foundation.component.config.KalendarDayConfig
import com.himanshoe.kalendar.foundation.event.KalendarEvents
import com.himanshoe.kalendar.foundation.event.KalendarEvent
import com.himanshoe.kalendar.foundation.ext.circleLayout
import com.himanshoe.kalendar.foundation.ext.dayBackgroundColor
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Renders a single day cell inside a calendar grid.
 *
 * The cell shows the day-of-month number and up to three event indicator dots below it.
 * Today's date is distinguished by a thin border ring even when not selected.
 * Disabled dates are rendered at 38 % opacity and do not respond to taps.
 *
 * @param date The [LocalDate] this cell represents.
 * @param modifier [Modifier] applied to the outermost layout.
 * @param selectedDates The list of currently selected dates used for multi-select
 *   ([OnDaySelectionAction.Multiple][com.himanshoe.kalendar.foundation.action.OnDaySelectionAction.Multiple])
 *   and range highlighting.
 * @param selectedRange The currently active date range, used to highlight the span between
 *   start and end dates inclusively.
 * @param selectedDate The single currently selected date used for
 *   [OnDaySelectionAction.Single][com.himanshoe.kalendar.foundation.action.OnDaySelectionAction.Single] mode.
 *   Defaults to [date] (no selection).
 * @param events The list of events that fall on [date]. Up to three indicator dots are shown;
 *   per-event colour from [KalendarEvent.eventColor] is used when available.
 * @param dayConfig Visual configuration (size, colours, text style) for the day cell.
 * @param isDisabled When `true` the cell is rendered at reduced opacity and taps are ignored.
 *   Typically driven by [com.himanshoe.kalendar.foundation.component.config.KalendarConfig.disabledDates].
 * @param onDayClick Callback invoked when the user taps an enabled cell. Receives the tapped
 *   [LocalDate] and the events on that date.
 */
@Composable
fun KalendarDay(
    date: LocalDate,
    modifier: Modifier = Modifier,
    selectedDates: List<LocalDate> = emptyList(),
    selectedRange: KalendarSelectedDayRange? = null,
    selectedDate: LocalDate = date,
    events: KalendarEvents = emptyList(),
    dayConfig: KalendarDayConfig = KalendarDayConfig(),
    isDisabled: Boolean = false,
    onDayClick: (LocalDate, List<KalendarEvent>) -> Unit = { _, _ -> },
) {
    KalendarDayContent(
        date = date,
        selectedDate = selectedDate,
        events = events,
        selectedRange = selectedRange,
        dayConfig = dayConfig,
        modifier = modifier,
        selectedDates = selectedDates,
        isDisabled = isDisabled,
        onDayClick = onDayClick,
    )
}

@Composable
private fun KalendarDayContent(
    date: LocalDate,
    modifier: Modifier = Modifier,
    selectedDates: List<LocalDate> = emptyList(),
    selectedRange: KalendarSelectedDayRange? = null,
    selectedDate: LocalDate = date,
    dayConfig: KalendarDayConfig = KalendarDayConfig(),
    events: KalendarEvents = emptyList(),
    isDisabled: Boolean = false,
    onDayClick: (LocalDate, List<KalendarEvent>) -> Unit = { _, _ -> }
) {
    val today = remember(TimeZone.currentSystemDefault()) {
        Clock.System.todayIn(TimeZone.currentSystemDefault())
    }
    val currentDay = today == date
    val selected = date == selectedDate || selectedDates.contains(date)
    val brush = remember(selected) {
        if (selected) {
            Brush.linearGradient(dayConfig.selectedTextColor.value)
        } else {
            dayConfig.textStyle.brush
        }
    }
    val fontWeight = remember(selected) { if (selected) FontWeight.Bold else FontWeight.Normal }

    Column(
        modifier = modifier
            .alpha(if (isDisabled) 0.38f else 1f)
            .border(
                border = getBorderStroke(
                    currentDay = currentDay,
                    brush = Brush.linearGradient(dayConfig.borderColor.value),
                    selected = selected
                ),
                shape = CircleShape
            )
            .clip(CircleShape)
            .dayBackgroundColor(
                selected = selected,
                selectedDates = selectedDates,
                date = date,
                selectedRange = selectedRange,
                colors = dayConfig.selectedBackgroundColor.value
            )
            .then(if (!isDisabled) Modifier.clickable { onDayClick(date, events) } else Modifier)
            .circleLayout()
            .defaultMinSize(dayConfig.size),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            modifier = Modifier.wrapContentSize(),
            textAlign = TextAlign.Center,
            style = dayConfig.textStyle.copy(
                brush = brush,
                fontWeight = fontWeight
            )
        )
        if (events.isNotEmpty()) {
            EventIndicators(
                events = events,
                dayConfig = dayConfig,
                modifier = Modifier.wrapContentSize().padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun EventIndicators(
    events: List<KalendarEvent>,
    dayConfig: KalendarDayConfig,
    modifier: Modifier = Modifier
) {
    val itemCount = minOf(events.size, 3)

    Row(modifier = modifier) {
        events.take(itemCount).fastForEachIndexed { index, event ->
            KalendarIndicator(
                modifier = Modifier,
                index = index,
                size = dayConfig.size,
                color = dayConfig.indicatorColor,
                overrideColor = event.eventColor,
            )
        }
    }
}

private fun getBorderStroke(
    currentDay: Boolean,
    brush: Brush,
    selected: Boolean
) = if (currentDay && !selected) {
    BorderStroke(1.dp, brush)
} else {
    BorderStroke(0.dp, Color.Transparent)
}
