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
import androidx.compose.foundation.layout.Box
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
    dayContent: (@Composable (date: LocalDate, isSelected: Boolean, events: List<KalendarEvent>) -> Unit)? = null,
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
    dayContent: (@Composable (date: LocalDate, isSelected: Boolean, events: List<KalendarEvent>) -> Unit)? = null,
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
        mutableStateOf(
            if (config.initialSelectedDates.isNotEmpty()) config.initialSelectedDates
            else listOf(selectedDate)
        )
    }
    val daysOfWeek = DayOfWeek.entries.rotate(startDayOfWeek.ordinal)
    val displayDates by remember(currentMonth, startDayOfWeek) {
        mutableStateOf(getMonthDates(currentMonth, startDayOfWeek))
    }
    val eventsByDate = remember(events) { events.groupBy { it.date } }

    val canGoBack = config.minDate?.let { min ->
        currentMonth > min.minus(min.dayOfMonth - 1, DateTimeUnit.DAY)
    } ?: true
    val canGoForward = config.maxDate?.let { max ->
        currentMonth < max.minus(max.dayOfMonth - 1, DateTimeUnit.DAY)
    } ?: true

    DisposableEffect(controller) {
        controller?.attachScrollImpl { date ->
            currentMonth = date.minus(date.dayOfMonth - 1, DateTimeUnit.DAY)
        }
        onDispose { controller?.detachScrollImpl() }
    }

    LaunchedEffect(currentMonth) {
        config.onVisibleRangeChange?.invoke(
            currentMonth,
            currentMonth.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
        )
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
            val isCurrentMonth = date.month == currentMonth.month
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
                    isDisabled = config.disabledDates(date) || !isCurrentMonth,
                )
            }
        }
    }
}

internal fun getMonthDates(
    currentMonth: LocalDate,
    startDayOfWeek: DayOfWeek
): List<LocalDate> {
    val firstDayOfMonth = currentMonth.minus(currentMonth.dayOfMonth - 1, DateTimeUnit.DAY)
    val lastDayOfMonth = firstDayOfMonth.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
    val firstDayOffset = (firstDayOfMonth.dayOfWeek.ordinal - startDayOfWeek.ordinal + 7) % 7
    return (-firstDayOffset until lastDayOfMonth.dayOfMonth).map {
        firstDayOfMonth.plus(it.toLong(), DateTimeUnit.DAY)
    }
}
