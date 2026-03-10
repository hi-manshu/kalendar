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

package com.himanshoe.kalendar.cosmic

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.himanshoe.kalendar.foundation.action.KalendarDateRange
import com.himanshoe.kalendar.foundation.component.KalendarHeader
import com.himanshoe.kalendar.foundation.component.config.KalendarDayKonfig
import com.himanshoe.kalendar.foundation.event.KalendarEvents
import com.himanshoe.kalendar.foundation.locale.KalendarLocale
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Displays a full year as a 4-row by 3-column grid of mini-month calendars.
 *
 * Each mini-month shows a compact header with the month name and a grid of day numbers.
 * Tapping on a mini-month triggers [onMonthClick], and tapping on an individual day
 * triggers [onDayClick].
 *
 * @param year The year to display.
 * @param selectedDate The currently selected date, highlighted across all mini-months.
 * @param events Calendar events used to show event indicators.
 * @param dayKonfig Configuration for day cell appearance.
 * @param kalendarLocale Locale for month and day name localisation.
 * @param startDayOfWeek The first day of each week column.
 * @param dateRange Date range constraints for enabled/disabled days.
 * @param onMonthClick Called when a mini-month header or body is tapped. Receives the [Month].
 * @param onDayClick Called when an individual day cell is tapped.
 * @param onPreviousYear Called when the user navigates to the previous year.
 * @param onNextYear Called when the user navigates to the next year.
 * @param modifier Modifier applied to the root layout.
 */
@Composable
internal fun CosmicYearGrid(
    year: Int,
    selectedDate: LocalDate,
    events: KalendarEvents,
    dayKonfig: KalendarDayKonfig,
    kalendarLocale: KalendarLocale,
    startDayOfWeek: DayOfWeek,
    dateRange: KalendarDateRange,
    onMonthClick: (Month) -> Unit,
    onDayClick: (LocalDate) -> Unit,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val months = remember { Month.entries.toList() }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        KalendarHeader(
            modifier = Modifier,
            title = year.toString(),
            arrowShown = true,
            onPreviousClick = onPreviousYear,
            onNextClick = onNextYear,
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(months) { month ->
                MiniMonth(
                    month = month,
                    year = year,
                    selectedDate = selectedDate,
                    today = today,
                    dayKonfig = dayKonfig,
                    kalendarLocale = kalendarLocale,
                    startDayOfWeek = startDayOfWeek,
                    dateRange = dateRange,
                    events = events,
                    onMonthClick = { onMonthClick(month) },
                    onDayClick = onDayClick,
                )
            }
        }
    }
}

/**
 * A single compact mini-month cell used inside [CosmicYearGrid].
 *
 * Shows a small month name header and a 7-column grid of day numbers.
 * The entire month is clickable to trigger drill-down, and individual
 * days can also be clicked.
 */
@Composable
private fun MiniMonth(
    month: Month,
    year: Int,
    selectedDate: LocalDate,
    today: LocalDate,
    dayKonfig: KalendarDayKonfig,
    kalendarLocale: KalendarLocale,
    startDayOfWeek: DayOfWeek,
    dateRange: KalendarDateRange,
    events: KalendarEvents,
    onMonthClick: () -> Unit,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val firstOfMonth = remember(year, month) { LocalDate(year, month, 1) }
    val displayDates = remember(firstOfMonth, startDayOfWeek) {
        getMonthDates(firstOfMonth, startDayOfWeek)
    }
    val daysOfWeek = remember(startDayOfWeek) {
        DayOfWeek.entries.rotate(startDayOfWeek.ordinal)
    }

    val monthName = remember(month, kalendarLocale) {
        kalendarLocale.monthNames.getOrElse(month.ordinal) {
            month.name.lowercase().replaceFirstChar { c -> c.uppercase() }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onMonthClick() }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = monthName,
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                brush = dayKonfig.textStyle.brush,
            ),
            modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
        )

        // Day-of-week labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            daysOfWeek.forEach { dow ->
                val label = kalendarLocale.shortDayNames.getOrElse(dow.ordinal) {
                    dow.name.take(1)
                }
                Text(
                    text = label.take(1),
                    style = TextStyle(
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                    ),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Day grid — 7 columns
        val rows = displayDates.chunked(7)
        rows.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                week.forEach { date ->
                    MiniDay(
                        date = date,
                        isCurrentMonth = date.month == month,
                        isToday = date == today,
                        isSelected = date == selectedDate,
                        isEnabled = dateRange.isDateEnabled(date),
                        dayKonfig = dayKonfig,
                        hasEvents = events.eventList.any { it.date == date },
                        onClick = { onDayClick(date) },
                        modifier = Modifier.weight(1f),
                    )
                }
                // Pad incomplete last row
                repeat(7 - week.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * A tiny day cell for the mini-month grid in the year view.
 *
 * Shows just the day number with minimal styling. Today is bordered,
 * the selected date has a background highlight, and out-of-month days
 * are shown with reduced opacity.
 */
@Composable
private fun MiniDay(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    isEnabled: Boolean,
    dayKonfig: KalendarDayKonfig,
    hasEvents: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val alpha = when {
        !isCurrentMonth -> 0.3f
        !isEnabled -> 0.5f
        else -> 1f
    }

    val bgModifier = if (isSelected && isCurrentMonth) {
        Modifier.background(
            brush = Brush.linearGradient(dayKonfig.selectedBackgroundColor.value),
            shape = CircleShape,
        )
    } else {
        Modifier
    }

    val borderModifier = if (isToday && isCurrentMonth && !isSelected) {
        Modifier.border(
            border = BorderStroke(0.5.dp, Brush.linearGradient(dayKonfig.borderColor.value)),
            shape = CircleShape,
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .alpha(alpha)
            .then(borderModifier)
            .then(bgModifier)
            .clip(CircleShape)
            .then(
                if (isEnabled && isCurrentMonth) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                },
            )
            .padding(1.dp)
            .size(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        val textBrush = if (isSelected && isCurrentMonth) {
            Brush.linearGradient(dayKonfig.selectedTextColor.value)
        } else {
            dayKonfig.textStyle.brush
        }

        Text(
            text = date.dayOfMonth.toString(),
            style = TextStyle(
                fontSize = 7.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                brush = textBrush,
            ),
        )

        if (hasEvents && isCurrentMonth) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size(2.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(dayKonfig.indicatorColor.value),
                    ),
            )
        }
    }
}
