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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.datetime.until

@Composable
internal fun KalendarAerial(
    selectedDate: LocalDate,
    modifier: Modifier = Modifier,
    events: KalendarEvents = emptyList(),
    onDaySelectionAction: OnDaySelectionAction = OnDaySelectionAction.NoOp,
    config: KalendarConfig = KalendarConfig(),
    controller: KalendarController? = null,
    dayContent: (@Composable (date: LocalDate, isSelected: Boolean, events: List<KalendarEvent>) -> Unit)? = null,
) {
    KalendarAerialContent(
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
private fun KalendarAerialContent(
    selectedDate: LocalDate,
    modifier: Modifier,
    onDaySelectionAction: OnDaySelectionAction,
    events: KalendarEvents,
    config: KalendarConfig,
    controller: KalendarController?,
    dayContent: (@Composable (date: LocalDate, isSelected: Boolean, events: List<KalendarEvent>) -> Unit)? = null,
) {
    val startDayOfWeek = config.startDayOfWeek
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val initialDate = config.firstVisibleDate ?: selectedDate

    var currentDay by remember { mutableStateOf(initialDate) }
    var rangeStartDate by remember {
        mutableStateOf<LocalDate?>(config.initialSelectedRange?.start)
    }
    var rangeEndDate by remember {
        mutableStateOf<LocalDate?>(config.initialSelectedRange?.endInclusive)
    }
    val coroutineScope = rememberCoroutineScope()
    val selectedRange = remember {
        mutableStateOf<KalendarSelectedDayRange?>(config.initialSelectedRange)
    }
    var clickedNewDate by remember { mutableStateOf(selectedDate) }
    var clickedNewDates by remember {
        mutableStateOf(
            if (config.initialSelectedDates.isNotEmpty()) config.initialSelectedDates
            else listOf(selectedDate)
        )
    }
    val daysOfWeek = DayOfWeek.entries.rotate(distance = startDayOfWeek.ordinal)
    val initialPageOffset = remember(initialDate, selectedDate) {
        selectedDate.until(initialDate, DateTimeUnit.DAY).toInt() / 7
    }
    val pagerState = rememberPagerState(
        initialPage = Int.MAX_VALUE / 2 + initialPageOffset,
        pageCount = { Int.MAX_VALUE }
    )
    val eventsByDate = remember(events) { events.groupBy { it.date } }
    val calendarIconEnabled = pagerState.currentPage != Int.MAX_VALUE / 2
    val headerText = remember(currentDay) {
        getWeekDates(currentDay = currentDay, startDayOfWeek = startDayOfWeek).buildHeaderText()
    }

    DisposableEffect(controller) {
        controller?.attachScrollImpl { date ->
            val dayDiff = selectedDate.until(date, DateTimeUnit.DAY)
            val weekOffset = dayDiff / 7
            val targetPage = Int.MAX_VALUE / 2 + weekOffset.toInt()
            pagerState.animateScrollToPage(targetPage)
        }
        onDispose { controller?.detachScrollImpl() }
    }

    Column(
        modifier = modifier.background(brush = Brush.linearGradient(config.backgroundColor.value)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        KalendarHeader(
            modifier = Modifier,
            title = headerText,
            showArrows = false,
            calendarIconEnabled = calendarIconEnabled,
            showCalendarIcon = true,
            onNavigateToday = {
                if (calendarIconEnabled) {
                    coroutineScope.launch {
                        currentDay = today
                        pagerState.animateScrollToPage(page = Int.MAX_VALUE / 2)
                    }
                }
            },
            headerConfig = config.headerConfig,
            canNavigateBack = true,
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val startDate = selectedDate.plus(
                value = (page - Int.MAX_VALUE / 2) * 7,
                unit = DateTimeUnit.DAY
            )
            val displayDates = getWeekDates(currentDay = startDate, startDayOfWeek = startDayOfWeek)

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

        LaunchedEffect(pagerState.currentPage) {
            val startDate = selectedDate.plus(
                value = (pagerState.currentPage - Int.MAX_VALUE / 2) * 7,
                unit = DateTimeUnit.DAY
            )
            currentDay = startDate
            val weekDates = getWeekDates(currentDay, startDayOfWeek)
            config.onVisibleRangeChange?.invoke(weekDates.first(), weekDates.last())
        }
    }
}
