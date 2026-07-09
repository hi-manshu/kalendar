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
import com.himanshoe.kalendar.foundation.action.onDayClick
import com.himanshoe.kalendar.foundation.component.KalendarDay
import com.himanshoe.kalendar.foundation.component.KalendarHeader
import com.himanshoe.kalendar.foundation.component.buildHeaderText
import com.himanshoe.kalendar.foundation.component.config.KalendarConfig
import com.himanshoe.kalendar.foundation.event.KalendarEvents
import com.himanshoe.kalendar.foundation.event.KalendarEvent
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import com.himanshoe.kalendar.foundation.action.isDateInteractable

@Composable
internal fun KalendarFirey(
    selectedDate: LocalDate,
    modifier: Modifier = Modifier,
    events: KalendarEvents = emptyList(),
    onDaySelectionAction: OnDaySelectionAction = OnDaySelectionAction.NoOp,
    config: KalendarConfig = KalendarConfig(),
    controller: KalendarController? = null,
    dayContent: (@Composable (LocalDate, Boolean, List<KalendarEvent>, Boolean) -> Unit)? = null,
) {
    KalendarFireyContent(
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
private fun KalendarFireyContent(
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
    var currentDay by remember { mutableStateOf(initialDate) }

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
    val displayDates by remember(currentDay, startDayOfWeek) {
        mutableStateOf(getWeekDates(currentDay, startDayOfWeek))
    }
    val eventsByDate = remember(events) { events.groupBy { it.date } }
    val multiSelectDates = when (onDaySelectionAction) {
        is OnDaySelectionAction.Multiple -> clickedNewDates
        else -> emptyList()
    }

    val canGoBack = config.minDate?.let { min ->
        getWeekDates(currentDay, startDayOfWeek).first() > min
    } ?: true
    val canGoForward = config.maxDate?.let { max ->
        getWeekDates(currentDay, startDayOfWeek).last() < max
    } ?: true

    DisposableEffect(controller) {
        val generation = controller?.attachScrollImpl { date ->
            currentDay = date
        }
        onDispose { generation?.let { controller?.detachScrollImpl(it) } }
    }

    LaunchedEffect(currentDay) {
        val weekDates = getWeekDates(currentDay, startDayOfWeek)
        config.onVisibleRangeChange?.invoke(weekDates.first(), weekDates.last())
    }

    Column(
        modifier = modifier.background(brush = Brush.linearGradient(config.backgroundColor.value)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        KalendarHeader(
            modifier = Modifier,
            title = displayDates.buildHeaderText(),
            showArrows = config.showArrows,
            showCalendarIcon = false,
            headerConfig = config.headerConfig,
            canNavigateBack = canGoBack,
            canNavigateForward = canGoForward,
            previousContentDescription = "Previous week",
            nextContentDescription = "Next week",
            onPreviousClick = {
                if (canGoBack) {
                    currentDay = currentDay.minus(7, DateTimeUnit.DAY)
                }
            },
            onNextClick = {
                if (canGoForward) {
                    currentDay = currentDay.plus(7, DateTimeUnit.DAY)
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
            val dateEvents = eventsByDate[date] ?: emptyList()
            val isDisabled = !date.isDateInteractable(config, isPrimaryPeriod = true)
            val isDateAllowed: (LocalDate) -> Boolean = { it.isDateInteractable(config) }
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

internal fun getWeekDates(currentDay: LocalDate, startDayOfWeek: DayOfWeek): List<LocalDate> {
    val startOfWeek = startOfWeekContaining(currentDay, startDayOfWeek)
    return (0..6).map { startOfWeek.plus(it, DateTimeUnit.DAY) }
}

internal fun List<DayOfWeek>.rotate(distance: Int): List<DayOfWeek> {
    return this.drop(distance) + this.take(distance)
}
