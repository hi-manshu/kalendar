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

@Composable
internal fun KalendarFirey(
    selectedDate: LocalDate,
    modifier: Modifier = Modifier,
    events: KalendarEvents = emptyList(),
    onDaySelectionAction: OnDaySelectionAction = OnDaySelectionAction.NoOp,
    config: KalendarConfig = KalendarConfig(),
    controller: KalendarController? = null,
    dayContent: (@Composable (date: LocalDate, isSelected: Boolean, events: List<KalendarEvent>) -> Unit)? = null,
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
    dayContent: (@Composable (date: LocalDate, isSelected: Boolean, events: List<KalendarEvent>) -> Unit)? = null,
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
        mutableStateOf(
            if (config.initialSelectedDates.isNotEmpty()) config.initialSelectedDates
            else listOf(selectedDate)
        )
    }
    val daysOfWeek = DayOfWeek.entries.rotate(startDayOfWeek.ordinal)
    val displayDates by remember(currentDay) {
        mutableStateOf(getWeekDates(currentDay, startDayOfWeek))
    }
    val eventsByDate = remember(events) { events.groupBy { it.date } }

    val canGoBack = config.minDate?.let { min ->
        currentDay.minus(7, DateTimeUnit.DAY) >= min
    } ?: true
    val canGoForward = config.maxDate?.let { max ->
        currentDay.plus(7, DateTimeUnit.DAY) <= max
    } ?: true

    DisposableEffect(controller) {
        controller?.attachScrollImpl { date ->
            currentDay = date
        }
        onDispose { controller?.detachScrollImpl() }
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
            if (dayContent != null) {
                val isSelected = date == clickedNewDate || clickedNewDates.contains(date)
                dayContent(date, isSelected, dateEvents)
            } else {
                KalendarDay(
                    date = date,
                    selectedRange = selectedRange.value,
                    selectedDates = clickedNewDates,
                    onDayClick = { clickedDate, clickedEvents: List<KalendarEvent> ->
                        clickedDate.onDayClick(
                            events = clickedEvents,
                            rangeStartDate = rangeStartDate,
                            rangeEndDate = rangeEndDate,
                            onDaySelectionAction = onDaySelectionAction,
                            onClickedNewDate = { clickedNewDate = it },
                            onMultipleClickedNewDate = { date ->
                                clickedNewDates = clickedNewDates.toMutableList().apply {
                                    if (clickedNewDates.contains(date)) remove(date) else add(date)
                                }
                            },
                            onClickedRangeStartDate = { rangeStartDate = it },
                            onClickedRangeEndDate = { rangeEndDate = it },
                            onUpdateSelectedRange = { selectedRange.value = it },
                        )
                    },
                    dayConfig = config.dayConfig,
                    events = dateEvents,
                    selectedDate = clickedNewDate,
                    isDisabled = config.disabledDates(date),
                )
            }
        }
    }
}

internal fun getWeekDates(currentDay: LocalDate, startDayOfWeek: DayOfWeek): List<LocalDate> {
    val startOfWeek = currentDay.minus(
        (currentDay.dayOfWeek.ordinal - startDayOfWeek.ordinal + 7) % 7,
        DateTimeUnit.DAY
    )
    return (0..6).map { startOfWeek.plus(it, DateTimeUnit.DAY) }
}

internal fun List<DayOfWeek>.rotate(distance: Int): List<DayOfWeek> {
    return this.drop(distance) + this.take(distance)
}
