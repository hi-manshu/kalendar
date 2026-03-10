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

package com.himanshoe.kalendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
import com.himanshoe.kalendar.foundation.component.config.KalendarHeaderKonfig
import com.himanshoe.kalendar.foundation.component.config.KalendarKonfig
import com.himanshoe.kalendar.foundation.event.KalendarEvents
import com.himanshoe.kalendar.foundation.event.KalenderEvent
import com.himanshoe.kalendar.foundation.locale.KalendarLocale
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

/**
 * A week-view calendar with arrow-based navigation between weeks.
 *
 * Displays a single week row where each cell is a day. Users can navigate
 * to the previous or next week using header arrow buttons.
 *
 * @param selectedDate The date to highlight as selected.
 * @param modifier Modifier applied to the root layout.
 * @param events Calendar events to display as indicators.
 * @param showDayLabel Whether to display the day-of-week label row.
 * @param arrowShown Whether to show navigation arrows in the header.
 * @param onDaySelectionAction Controls how day selection works.
 * @param kalendarKonfig Visual configuration for the calendar.
 * @param restrictToCurrentWeek When true, prevents navigating before the current week.
 * @param startDayOfWeek The first day of the week column.
 * @param dateRange Date range constraints for enabled/disabled days.
 */
@Composable
internal fun KalendarFirey(
    selectedDate: LocalDate,
    modifier: Modifier = Modifier,
    events: KalendarEvents = KalendarEvents(),
    showDayLabel: Boolean = true,
    arrowShown: Boolean = true,
    onDaySelectionAction: OnDaySelectionAction = OnDaySelectionAction.Single { _, _ -> },
    kalendarKonfig: KalendarKonfig = KalendarKonfig(),
    restrictToCurrentWeek: Boolean = false,
    startDayOfWeek: DayOfWeek = DayOfWeek.SUNDAY,
    dateRange: KalendarDateRange = KalendarDateRange(),
) {
    KalendarFireyContent(
        selectedDate = selectedDate,
        modifier = modifier,
        arrowShown = arrowShown,
        showDayLabel = showDayLabel,
        onDaySelectionAction = onDaySelectionAction,
        dayKonfig = kalendarKonfig.kalendarDayKonfig,
        kalendarHeaderKonfig = kalendarKonfig.kalendarHeaderKonfig,
        kalendarDayLabelKonfig = kalendarKonfig.kalendarDayLabelKonfig,
        restrictToCurrentWeek = restrictToCurrentWeek,
        events = events,
        backgroundColor = kalendarKonfig.backgroundColor,
        startDayOfWeek = startDayOfWeek,
        dateRange = dateRange,
        kalendarLocale = kalendarKonfig.kalendarLocale,
    )
}

@Composable
private fun KalendarFireyContent(
    selectedDate: LocalDate,
    arrowShown: Boolean,
    backgroundColor: KalendarColor,
    showDayLabel: Boolean,
    onDaySelectionAction: OnDaySelectionAction,
    dayKonfig: KalendarDayKonfig,
    events: KalendarEvents,
    kalendarHeaderKonfig: KalendarHeaderKonfig,
    kalendarDayLabelKonfig: KalendarDayLabelKonfig,
    modifier: Modifier = Modifier,
    restrictToCurrentWeek: Boolean = false,
    startDayOfWeek: DayOfWeek = DayOfWeek.SUNDAY,
    dateRange: KalendarDateRange = KalendarDateRange(),
    kalendarLocale: KalendarLocale = KalendarLocale.default(),
) {
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val startOfWeek = remember(today) { today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY) }
    var currentDay by remember { mutableStateOf(today) }

    val selectedRange = remember { mutableStateOf<KalendarSelectedDayRange?>(null) }
    var rangeStartDate by remember { mutableStateOf<LocalDate?>(null) }
    var rangeEndDate by remember { mutableStateOf<LocalDate?>(null) }

    var clickedNewDate by remember { mutableStateOf(selectedDate) }
    val daysOfWeek = DayOfWeek.entries.rotate(startDayOfWeek.ordinal)
    val displayDates by remember(currentDay) {
        mutableStateOf(getWeekDates(currentDay, startDayOfWeek))
    }
    var clickedNewDates by remember { mutableStateOf(listOf(selectedDate)) }

    Column(
        modifier = modifier.background(brush = Brush.linearGradient(backgroundColor.value)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        KalendarHeader(
            modifier = Modifier,
            title = displayDates.buildHeaderText(kalendarLocale),
            arrowShown = arrowShown,
            showCalendarIcon = false,
            kalendarHeaderKonfig = kalendarHeaderKonfig,
            canNavigateBack = !restrictToCurrentWeek || currentDay > today,
            onPreviousClick = {
                val newDay = currentDay.minus(7, DateTimeUnit.DAY)
                if (!restrictToCurrentWeek || newDay >= startOfWeek) {
                    currentDay = newDay
                }
            },
            onNextClick = { currentDay = currentDay.plus(7, DateTimeUnit.DAY) }
        )
        KalendarScaffold(
            modifier = Modifier.fillMaxWidth(),
            showDayLabel = showDayLabel,
            dayOfWeek = { daysOfWeek },
            kalendarDayLabelKonfig = kalendarDayLabelKonfig,
            kalendarLocale = kalendarLocale,
            dates = { displayDates },
        ) { date ->
            KalendarDay(
                date = date,
                selectedRange = selectedRange.value,
                selectedDates = clickedNewDates,
                onDayClick = { clickedDate, events: List<KalenderEvent> ->
                    clickedDate.onDayClick(
                        events = events,
                        rangeStartDate = rangeStartDate,
                        rangeEndDate = rangeEndDate,
                        onDaySelectionAction = onDaySelectionAction,
                        onClickedNewDate = {
                            clickedNewDate = it
                        },
                        onMultipleClickedNewDate = { _clickedDate ->
                            clickedNewDates = clickedNewDates.toMutableList().apply {
                                if (clickedNewDates.contains(_clickedDate)) {
                                    remove(_clickedDate)
                                } else {
                                    add(_clickedDate)
                                }
                            }
                        },
                        onClickedRangeStartDate = {
                            rangeStartDate = it
                        },
                        onClickedRangeEndDate = {
                            rangeEndDate = it
                        },
                        onUpdateSelectedRange = {
                            selectedRange.value = it
                        },
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

/**
 * Computes the seven dates of the week containing [currentDay],
 * starting from the given [startDayOfWeek].
 *
 * @param currentDay Any date within the desired week.
 * @param startDayOfWeek The day of the week to use as the first column.
 * @return A list of seven [LocalDate] values for the week.
 */
internal fun getWeekDates(currentDay: LocalDate, startDayOfWeek: DayOfWeek): List<LocalDate> {
    val startOfWeek = currentDay.minus(
        (currentDay.dayOfWeek.ordinal - startDayOfWeek.ordinal + 7) % 7,
        DateTimeUnit.DAY
    )
    return (0..6).map { startOfWeek.plus(it, DateTimeUnit.DAY) }
}

/**
 * Rotates this list of [DayOfWeek] so that the element at [distance] becomes the first element.
 *
 * @param distance The number of positions to rotate.
 * @return A new list with the elements rotated.
 */
internal fun List<DayOfWeek>.rotate(distance: Int): List<DayOfWeek> {
    return this.drop(distance) + this.take(distance)
}
