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
import com.himanshoe.kalendar.foundation.component.config.KalendarConfig
import com.himanshoe.kalendar.foundation.event.KalendarEvents
import com.himanshoe.kalendar.foundation.event.KalendarEvent
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

@Composable
internal fun KalendarSolaris(
    selectedDate: LocalDate,
    modifier: Modifier = Modifier,
    events: KalendarEvents = emptyList(),
    onDaySelectionAction: OnDaySelectionAction = OnDaySelectionAction.NoOp,
    config: KalendarConfig = KalendarConfig(),
    controller: KalendarController? = null,
    dayContent: (@Composable (date: LocalDate, isSelected: Boolean, events: List<KalendarEvent>) -> Unit)? = null,
) {
    KalendarSolarisContent(
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
private fun KalendarSolarisContent(
    selectedDate: LocalDate,
    onDaySelectionAction: OnDaySelectionAction,
    events: KalendarEvents,
    config: KalendarConfig,
    modifier: Modifier = Modifier,
    controller: KalendarController? = null,
    dayContent: (@Composable (date: LocalDate, isSelected: Boolean, events: List<KalendarEvent>) -> Unit)? = null,
) {
    val startDayOfWeek = config.startDayOfWeek
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
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

    val initialMonthOffset = remember(initialDate, selectedDate) {
        val selectedMonthStart = selectedDate.minus(selectedDate.dayOfMonth - 1, DateTimeUnit.DAY)
        val initialMonthStart = initialDate.minus(initialDate.dayOfMonth - 1, DateTimeUnit.DAY)
        (initialMonthStart.year - selectedMonthStart.year) * 12 +
            (initialMonthStart.monthNumber - selectedMonthStart.monthNumber)
    }
    val pagerState = rememberPagerState(
        initialPage = Int.MAX_VALUE / 2 + initialMonthOffset,
        pageCount = { Int.MAX_VALUE }
    )
    val coroutineScope = rememberCoroutineScope()
    val calendarIconEnabled = pagerState.currentPage != Int.MAX_VALUE / 2

    DisposableEffect(controller) {
        controller?.attachScrollImpl { date ->
            val targetMonthStart = date.minus(date.dayOfMonth - 1, DateTimeUnit.DAY)
            val selectedMonthStart = selectedDate.minus(selectedDate.dayOfMonth - 1, DateTimeUnit.DAY)
            val monthDiff = (targetMonthStart.year - selectedMonthStart.year) * 12 +
                (targetMonthStart.monthNumber - selectedMonthStart.monthNumber)
            val targetPage = Int.MAX_VALUE / 2 + monthDiff
            pagerState.animateScrollToPage(targetPage)
        }
        onDispose { controller?.detachScrollImpl() }
    }

    Column(
        modifier = modifier.background(brush = Brush.linearGradient(colors = config.backgroundColor.value)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        KalendarHeader(
            modifier = Modifier,
            month = currentMonth.month,
            year = currentMonth.year,
            showCalendarIcon = true,
            showArrows = false,
            calendarIconEnabled = calendarIconEnabled,
            onNavigateToday = {
                if (calendarIconEnabled) {
                    coroutineScope.launch {
                        currentMonth = today.minus(today.dayOfMonth - 1, DateTimeUnit.DAY)
                        pagerState.animateScrollToPage(page = Int.MAX_VALUE / 2)
                    }
                }
            }
        )
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) {
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
    LaunchedEffect(pagerState.currentPage) {
        val startDate = selectedDate.plus(
            value = (pagerState.currentPage - Int.MAX_VALUE / 2),
            unit = DateTimeUnit.MONTH
        )
        currentMonth = startDate
        config.onVisibleRangeChange?.invoke(
            currentMonth,
            currentMonth.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
        )
    }
}
