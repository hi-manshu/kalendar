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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.himanshoe.kalendar.foundation.KalendarScaffold
import com.himanshoe.kalendar.foundation.action.KalendarDateRange
import com.himanshoe.kalendar.foundation.action.KalendarSelectedDayRange
import com.himanshoe.kalendar.foundation.action.OnDaySelectionAction
import com.himanshoe.kalendar.foundation.action.onDayClick
import com.himanshoe.kalendar.foundation.color.KalendarColor
import com.himanshoe.kalendar.foundation.component.KalendarDay
import com.himanshoe.kalendar.foundation.component.KalendarHeader
import com.himanshoe.kalendar.foundation.component.buildHeaderText
import com.himanshoe.kalendar.foundation.component.config.KalendarDayKonfig
import com.himanshoe.kalendar.foundation.component.config.KalendarDayLabelKonfig
import com.himanshoe.kalendar.foundation.event.KalendarEvents
import com.himanshoe.kalendar.foundation.event.KalenderEvent
import com.himanshoe.kalendar.foundation.locale.KalendarLocale
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

/**
 * A week view used as the deepest drill-down level from [CosmicMonthView].
 *
 * Renders a single week row with full day cells, event indicators, and day selection.
 * Includes a back button to return to the month view.
 *
 * @param weekStartDate The first date of the week to display.
 * @param selectedDate The currently selected date.
 * @param events Calendar events for the week.
 * @param onDaySelectionAction The day selection mode (single, multiple, or range).
 * @param dayKonfig Day cell appearance configuration.
 * @param kalendarDayLabelKonfig Day label header configuration.
 * @param kalendarLocale Locale for day name localisation.
 * @param backgroundColor Background color of the week view.
 * @param startDayOfWeek The first day of the week.
 * @param dateRange Date range constraints.
 * @param onBack Called when the user presses the back button to return to month view.
 * @param modifier Modifier applied to the root layout.
 */
@Composable
internal fun CosmicWeekView(
    weekStartDate: LocalDate,
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
    modifier: Modifier = Modifier,
) {
    val daysOfWeek = remember(startDayOfWeek) {
        DayOfWeek.entries.rotate(startDayOfWeek.ordinal)
    }
    val displayDates = remember(weekStartDate, startDayOfWeek) {
        getWeekDates(weekStartDate, startDayOfWeek)
    }

    val selectedRange = remember { mutableStateOf<KalendarSelectedDayRange?>(null) }
    var rangeStartDate by remember { mutableStateOf<LocalDate?>(null) }
    var rangeEndDate by remember { mutableStateOf<LocalDate?>(null) }
    var clickedNewDate by remember { mutableStateOf(selectedDate) }
    var clickedNewDates by remember { mutableStateOf(listOf(selectedDate)) }

    val headerText = remember(displayDates, kalendarLocale) {
        displayDates.buildHeaderText(kalendarLocale)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(brush = Brush.linearGradient(backgroundColor.value)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Back button + week header
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to month view",
                )
            }
            KalendarHeader(
                modifier = Modifier.weight(1f),
                title = headerText,
                arrowShown = false,
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
            KalendarDay(
                date = date,
                selectedRange = selectedRange.value,
                selectedDates = clickedNewDates,
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
        }
    }
}
