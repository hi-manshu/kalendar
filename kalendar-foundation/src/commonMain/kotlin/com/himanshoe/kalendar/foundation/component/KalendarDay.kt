/*
 *
 *  * Copyright 2025 Kalendar Contributors (https://www.himanshoe.com). All rights reserved.
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.kalendar.foundation.action.KalendarDateRange
import com.himanshoe.kalendar.foundation.action.KalendarSelectedDayRange
import com.himanshoe.kalendar.foundation.component.config.KalendarDayKonfig
import com.himanshoe.kalendar.foundation.event.KalendarEvents
import com.himanshoe.kalendar.foundation.event.KalenderEvent
import com.himanshoe.kalendar.foundation.ext.circleLayout
import com.himanshoe.kalendar.foundation.ext.dayBackgroundColor
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@Composable
fun KalendarDay(
    date: LocalDate,
    modifier: Modifier = Modifier,
    selectedDates: List<LocalDate> = emptyList(),
    selectedRange: KalendarSelectedDayRange? = null,
    selectedDate: LocalDate = date,
    events: KalendarEvents = KalendarEvents(),
    dayKonfig: KalendarDayKonfig = KalendarDayKonfig.default(),
    dateRange: KalendarDateRange = KalendarDateRange(),
    onDayClick: (LocalDate, List<KalenderEvent>) -> Unit = { _, _ -> },
) {
    KalendarDayContent(
        date = date,
        selectedDate = selectedDate,
        events = events,
        selectedRange = selectedRange,
        dayKonfig = dayKonfig,
        dateRange = dateRange,
        modifier = modifier,
        selectedDates = selectedDates,
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
    dayKonfig: KalendarDayKonfig = KalendarDayKonfig.default(),
    dateRange: KalendarDateRange = KalendarDateRange(),
    events: KalendarEvents = KalendarEvents(),
    onDayClick: (LocalDate, List<KalenderEvent>) -> Unit = { _, _ -> }
) {
    val today = remember(TimeZone.currentSystemDefault()) {
        Clock.System.todayIn(TimeZone.currentSystemDefault())
    }
    val currentDay = today == date
    val isEnabled = remember(date, dateRange) { dateRange.isDateEnabled(date) }
    val selected = isEnabled && (date == selectedDate || selectedDates.contains(date))
    val brush = remember(selected, isEnabled) {
        when {
            !isEnabled -> Brush.linearGradient(dayKonfig.disabledTextColor.value)
            selected -> Brush.linearGradient(dayKonfig.selectedTextColor.value)
            else -> dayKonfig.textStyle.brush
        }
    }
    val fontWeight = remember(selected) { if (selected) FontWeight.Bold else FontWeight.Normal }
    val currentDayEvents = remember(events) { events.eventList.fastFilter { it.date == date } }

    val dayDescription = remember(date, selected, currentDay, isEnabled, currentDayEvents.size) {
        buildString {
            append("${date.dayOfMonth}")
            if (currentDay) append(", today")
            if (selected) append(", selected")
            if (!isEnabled) append(", disabled")
            if (currentDayEvents.isNotEmpty()) {
                append(", ${currentDayEvents.size} event${if (currentDayEvents.size > 1) "s" else ""}")
            }
        }
    }

    Column(
        modifier = modifier
            .semantics {
                contentDescription = dayDescription
                role = Role.Button
                this.selected = selected
                if (!isEnabled) disabled()
            }
            .alpha(if (isEnabled) 1f else 0.5f)
            .border(
                border = getBorderStroke(
                    currentDay = currentDay,
                    brush = Brush.linearGradient(dayKonfig.borderColor.value),
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
                colors = dayKonfig.selectedBackgroundColor.value
            )
            .then(
                if (isEnabled) {
                    Modifier.clickable { onDayClick(date, currentDayEvents) }
                } else {
                    Modifier
                }
            )
            .circleLayout()
            .defaultMinSize(dayKonfig.size),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            modifier = Modifier.wrapContentSize(),
            textAlign = TextAlign.Center,
            style = dayKonfig.textStyle.copy(
                brush = brush,
                fontWeight = fontWeight
            )
        )
        if (currentDayEvents.isNotEmpty()) {
            EventIndicators(
                events = currentDayEvents,
                dayKonfig = dayKonfig,
                modifier = modifier.wrapContentSize().padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun EventIndicators(
    events: List<KalenderEvent>,
    dayKonfig: KalendarDayKonfig,
    modifier: Modifier = Modifier
) {
    val itemCount = if (events.count() > 3) 3 else events.size

    Row(modifier = modifier) {
        events.take(itemCount).fastForEachIndexed { index, event ->
            KalendarIndicator(
                modifier = Modifier,
                index = index,
                size = dayKonfig.size,
                color = dayKonfig.indicatorColor,
                eventColor = event.eventColor,
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
