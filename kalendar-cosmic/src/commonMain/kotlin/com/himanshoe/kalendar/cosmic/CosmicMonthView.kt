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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.himanshoe.kalendar.foundation.KalendarScaffold
import com.himanshoe.kalendar.foundation.action.KalendarDateRange
import com.himanshoe.kalendar.foundation.action.KalendarSelectedDayRange
import com.himanshoe.kalendar.foundation.action.OnDaySelectionAction
import com.himanshoe.kalendar.foundation.action.onDayClick
import com.himanshoe.kalendar.foundation.color.KalendarColor
import com.himanshoe.kalendar.foundation.component.KalendarDay
import com.himanshoe.kalendar.foundation.component.KalendarHeader
import com.himanshoe.kalendar.foundation.component.config.KalendarDayKonfig
import com.himanshoe.kalendar.foundation.component.config.KalendarDayLabelKonfig
import com.himanshoe.kalendar.foundation.event.KalendarEvents
import com.himanshoe.kalendar.foundation.event.KalenderEvent
import com.himanshoe.kalendar.foundation.locale.KalendarLocale
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * A full month view used as the drill-down target from [CosmicYearGrid].
 *
 * Renders a complete month grid with day labels, event indicators, and day selection.
 * Includes a back button to return to the year view, and optionally supports
 * drilling further into a week view when [onWeekClick] is provided.
 *
 * @param month The month to display.
 * @param year The year of the month.
 * @param selectedDate The currently selected date.
 * @param events Calendar events for the month.
 * @param onDaySelectionAction The day selection mode (single, multiple, or range).
 * @param dayKonfig Day cell appearance configuration.
 * @param kalendarDayLabelKonfig Day label header configuration.
 * @param kalendarLocale Locale for month and day name localisation.
 * @param backgroundColor Background color of the month view.
 * @param startDayOfWeek The first day of the week.
 * @param dateRange Date range constraints.
 * @param onBack Called when the user presses the back button to return to year view.
 * @param onWeekClick Called when the user taps a week row, receiving the first date of that week.
 *   Null if week drill-down is disabled.
 * @param modifier Modifier applied to the root layout.
 */
@Composable
internal fun CosmicMonthView(
    month: Month,
    year: Int,
    selectedDate: LocalDate,
    events: KalendarEvents,
    onDaySelectionAction: OnDaySelectionAction,
    dayKonfig: KalendarDayKonfig,
    kalendarDayLabelKonfig: KalendarDayLabelKonfig,
    kalendarLocale: KalendarLocale,
    backgroundColor: KalendarColor,
    startDayOfWeek: DayOfWeek,
    dateRange: KalendarDateRange,
    onBack: () -> Unit,
    onWeekClick: ((LocalDate) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val firstOfMonth = remember(year, month) { LocalDate(year, month, 1) }
    val daysOfWeek = remember(startDayOfWeek) {
        DayOfWeek.entries.rotate(startDayOfWeek.ordinal)
    }
    val displayDates = remember(firstOfMonth, startDayOfWeek) {
        getMonthDates(firstOfMonth, startDayOfWeek)
    }

    val selectedRange = remember { mutableStateOf<KalendarSelectedDayRange?>(null) }
    var rangeStartDate by remember { mutableStateOf<LocalDate?>(null) }
    var rangeEndDate by remember { mutableStateOf<LocalDate?>(null) }
    var clickedNewDate by remember { mutableStateOf(selectedDate) }
    var clickedNewDates by remember { mutableStateOf(listOf(selectedDate)) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(brush = Brush.linearGradient(backgroundColor.value)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Back button + month/year header
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to year view",
                )
            }
            KalendarHeader(
                modifier = Modifier.weight(1f),
                month = month,
                year = year,
                arrowShown = false,
                kalendarLocale = kalendarLocale,
            )
        }

        KalendarScaffold(
            modifier = Modifier.fillMaxWidth(),
            showDayLabel = true,
            dayOfWeek = { daysOfWeek },
            kalendarDayLabelKonfig = kalendarDayLabelKonfig,
            kalendarLocale = kalendarLocale,
            dates = { displayDates },
        ) { date ->
            if (date.month == month) {
                val weekModifier = if (onWeekClick != null) {
                    val weekStart = remember(date, startDayOfWeek) {
                        getWeekDates(date, startDayOfWeek).first()
                    }
                    Modifier.clickable { onWeekClick(weekStart) }
                } else {
                    Modifier
                }

                KalendarDay(
                    date = date,
                    selectedRange = selectedRange.value,
                    selectedDates = clickedNewDates,
                    modifier = weekModifier,
                    onDayClick = { clickedDate, dayEvents: List<KalenderEvent> ->
                        clickedDate.onDayClick(
                            events = dayEvents,
                            rangeStartDate = rangeStartDate,
                            rangeEndDate = rangeEndDate,
                            onDaySelectionAction = onDaySelectionAction,
                            onClickedNewDate = { clickedNewDate = it },
                            onMultipleClickedNewDate = { d ->
                                clickedNewDates = clickedNewDates.toMutableList().apply {
                                    if (contains(d)) remove(d) else add(d)
                                }
                            },
                            onClickedRangeStartDate = { rangeStartDate = it },
                            onClickedRangeEndDate = { rangeEndDate = it },
                            onUpdateSelectedRange = { selectedRange.value = it },
                        )
                    },
                    dayKonfig = dayKonfig,
                    dateRange = dateRange,
                    events = events,
                    selectedDate = clickedNewDate,
                )
            } else {
                Box(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
