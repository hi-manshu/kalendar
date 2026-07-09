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

package com.himanshoe.kalendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.himanshoe.kalendar.foundation.KalendarScaffold
import com.himanshoe.kalendar.foundation.action.KalendarSelectedDayRange
import com.himanshoe.kalendar.foundation.action.OnDaySelectionAction
import com.himanshoe.kalendar.foundation.action.isDateInteractable
import com.himanshoe.kalendar.foundation.action.onDayClick
import com.himanshoe.kalendar.foundation.component.KalendarDay
import com.himanshoe.kalendar.foundation.component.KalendarHeader
import com.himanshoe.kalendar.foundation.component.config.KalendarConfig
import com.himanshoe.kalendar.foundation.event.KalendarEvents
import com.himanshoe.kalendar.foundation.event.KalendarEvent
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

@Composable
internal fun KalendarOceanic(
    selectedDate: LocalDate,
    config: KalendarConfig,
    events: KalendarEvents,
    modifier: Modifier = Modifier,
    controller: KalendarController? = null,
    onDaySelectionAction: OnDaySelectionAction = OnDaySelectionAction.NoOp,
    dayContent: (@Composable (LocalDate, Boolean, List<KalendarEvent>, Boolean) -> Unit)? = null,
) {
    KalendarOceanicContent(
        selectedDate = selectedDate,
        modifier = modifier,
        onDaySelectionAction = onDaySelectionAction,
        events = events,
        config = config,
        controller = controller,
        dayContent = dayContent,
    )
}

@Composable
private fun KalendarOceanicContent(
    selectedDate: LocalDate,
    onDaySelectionAction: OnDaySelectionAction,
    events: KalendarEvents,
    config: KalendarConfig,
    modifier: Modifier = Modifier,
    controller: KalendarController? = null,
    dayContent: (@Composable (LocalDate, Boolean, List<KalendarEvent>, Boolean) -> Unit)? = null,
) {
    val startDayOfWeek = config.startDayOfWeek
    val initialDate = config.firstVisibleDate ?: selectedDate
    var currentMonth by remember {
        mutableStateOf(initialDate.minus(initialDate.dayOfMonth - 1, DateTimeUnit.DAY))
    }
    val selectedRange = remember {
        mutableStateOf<KalendarSelectedDayRange?>(config.initialSelectedRange)
    }
    var rangeStartDate by remember {
        mutableStateOf<LocalDate?>(config.initialSelectedRange?.start)
    }
    var rangeEndDate by remember {
        mutableStateOf<LocalDate?>(config.initialSelectedRange?.endInclusive)
    }
    var clickedNewDate by remember { mutableStateOf(selectedDate) }
    var clickedNewDates by remember {
        mutableStateOf(config.initialSelectedDates)
    }
    LaunchedEffect(selectedDate) {
        clickedNewDate = selectedDate
    }
    val daysOfWeek = DayOfWeek.entries.rotate(startDayOfWeek.ordinal)
    val displayDates by remember(currentMonth, startDayOfWeek) {
        mutableStateOf(getMonthDates(currentMonth, startDayOfWeek))
    }
    val eventsByDate = remember(events) { events.groupBy { it.date } }
    val multiSelectDates = when (onDaySelectionAction) {
        is OnDaySelectionAction.Multiple -> clickedNewDates
        else -> emptyList()
    }

    val canGoBack = config.minDate?.let { min ->
        currentMonth > min.minus(min.dayOfMonth - 1, DateTimeUnit.DAY)
    } ?: true
    val canGoForward = config.maxDate?.let { max ->
        currentMonth < max.minus(max.dayOfMonth - 1, DateTimeUnit.DAY)
    } ?: true

    DisposableEffect(controller) {
        val generation = controller?.attachScrollImpl { date ->
            currentMonth = date.minus(date.dayOfMonth - 1, DateTimeUnit.DAY)
        }
        onDispose { generation?.let { controller?.detachScrollImpl(it) } }
    }

    LaunchedEffect(currentMonth, startDayOfWeek) {
        val grid = getMonthDates(currentMonth, startDayOfWeek)
        config.onVisibleRangeChange?.invoke(grid.first(), grid.last())
    }

    Column(
        modifier = modifier.background(brush = Brush.linearGradient(config.backgroundColor.value)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        KalendarHeader(
            modifier = Modifier,
            month = currentMonth.month,
            year = currentMonth.year,
            showArrows = config.showArrows,
            canNavigateBack = canGoBack,
            canNavigateForward = canGoForward,
            previousContentDescription = "Previous month",
            nextContentDescription = "Next month",
            onPreviousClick = {
                if (canGoBack) {
                    currentMonth = currentMonth.minus(1, DateTimeUnit.MONTH)
                }
            },
            onNextClick = {
                if (canGoForward) {
                    currentMonth = currentMonth.plus(1, DateTimeUnit.MONTH)
                }
            }
        )
        KalendarScaffold(
            modifier = Modifier.fillMaxWidth(),
            showDayLabel = config.showDayLabel,
            dayOfWeek = { daysOfWeek },
            dayLabelConfig = config.dayLabelConfig,
            dates = { displayDates },
        ) { date ->
            val isCurrentMonth =
                date.year == currentMonth.year && date.month == currentMonth.month
            val dateEvents = eventsByDate[date] ?: emptyList()
            val isDisabled = !date.isDateInteractable(config, isPrimaryPeriod = isCurrentMonth)
            val isDateAllowed: (LocalDate) -> Boolean = { candidate ->
                val primary =
                    candidate.year == currentMonth.year && candidate.month == currentMonth.month
                candidate.isDateInteractable(config, isPrimaryPeriod = primary)
            }
            if (dayContent != null) {
                val isSelected = when (onDaySelectionAction) {
                    is OnDaySelectionAction.Multiple -> date in multiSelectDates
                    is OnDaySelectionAction.Range ->
                        selectedRange.value?.let { date in it } == true || date == clickedNewDate
                    else -> date == clickedNewDate
                }
                dayContent(date, isSelected, dateEvents, isDisabled)
            } else {
                KalendarDay(
                    date = date,
                    selectedRange = selectedRange.value,
                    selectedDates = multiSelectDates,
                    onDayClick = { clickedDate, clickedEvents: List<KalendarEvent> ->
                        clickedDate.onDayClick(
                            events = clickedEvents,
                            allEvents = events,
                            rangeStartDate = rangeStartDate,
                            rangeEndDate = rangeEndDate,
                            onDaySelectionAction = onDaySelectionAction,
                            onClickedNewDate = { clickedNewDate = it },
                            onMultipleClickedNewDate = { tapped ->
                                clickedNewDates = clickedNewDates.toMutableList().apply {
                                    if (contains(tapped)) remove(tapped) else add(tapped)
                                }
                            },
                            onClickedRangeStartDate = { rangeStartDate = it },
                            onClickedRangeEndDate = { rangeEndDate = it },
                            onUpdateSelectedRange = { selectedRange.value = it },
                            isDateAllowed = isDateAllowed,
                        )
                    },
                    dayConfig = config.dayConfig,
                    events = dateEvents,
                    selectedDate = clickedNewDate,
                    isDisabled = isDisabled,
                )
            }
        }
    }
}

internal fun isDateOutOfBounds(
    date: LocalDate,
    minDate: LocalDate?,
    maxDate: LocalDate?,
): Boolean {
    val beforeMin = minDate != null && date < minDate
    val afterMax = maxDate != null && date > maxDate
    return beforeMin || afterMax
}

/**
 * Returns the first day of the week that contains [date], according to [startDayOfWeek].
 *
 * When [date] is the 1st of a month and that 1st is not [startDayOfWeek], the result lands
 * in the **previous calendar month** (and possibly the previous year). Example with
 * Monday-first weeks: 1 Jan 2026 is a Thursday → this returns Mon 29 Dec 2025.
 */
internal fun startOfWeekContaining(
    date: LocalDate,
    startDayOfWeek: DayOfWeek,
): LocalDate {
    val daysBack = (date.dayOfWeek.ordinal - startDayOfWeek.ordinal + 7) % 7
    return date.minus(daysBack, DateTimeUnit.DAY)
}

/**
 * Builds the full month grid for [currentMonth] (any day in that month is accepted as anchor).
 *
 * The first cell is always [startOfWeekContaining] the 1st of the month — so when the 1st is
 * not the configured week start, leading cells are dates from the previous calendar month.
 * Trailing cells fill through the end of the week that contains the last day of the month.
 * Length is always a multiple of 7.
 */
internal fun getMonthDates(
    currentMonth: LocalDate,
    startDayOfWeek: DayOfWeek,
): List<LocalDate> {
    val firstDayOfMonth = currentMonth.minus(currentMonth.dayOfMonth - 1, DateTimeUnit.DAY)
    val lastDayOfMonth = firstDayOfMonth.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
    val gridStart = startOfWeekContaining(firstDayOfMonth, startDayOfWeek)
    val gridEnd = startOfWeekContaining(lastDayOfMonth, startDayOfWeek)
        .plus(6, DateTimeUnit.DAY)

    val dates = ArrayList<LocalDate>(42)
    var cursor = gridStart
    while (cursor <= gridEnd) {
        dates += cursor
        cursor = cursor.plus(1, DateTimeUnit.DAY)
    }
    return dates
}
