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
import com.himanshoe.kalendar.foundation.action.isDateInteractable

@Composable
internal fun KalendarSolaris(
    selectedDate: LocalDate,
    modifier: Modifier = Modifier,
    events: KalendarEvents = emptyList(),
    onDaySelectionAction: OnDaySelectionAction = OnDaySelectionAction.NoOp,
    config: KalendarConfig = KalendarConfig(),
    controller: KalendarController? = null,
    dayContent: (@Composable (LocalDate, Boolean, List<KalendarEvent>, Boolean) -> Unit)? = null,
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
    dayContent: (@Composable (LocalDate, Boolean, List<KalendarEvent>, Boolean) -> Unit)? = null,
) {
    val startDayOfWeek = config.startDayOfWeek
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val todayMonthStart = remember(today) {
        today.minus(today.dayOfMonth - 1, DateTimeUnit.DAY)
    }
    val initialDate = config.firstVisibleDate ?: selectedDate
    val initialMonthStart = remember(initialDate) {
        initialDate.minus(initialDate.dayOfMonth - 1, DateTimeUnit.DAY)
    }
    var currentMonth by remember { mutableStateOf(initialMonthStart) }
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
    var clickedNewDates by remember { mutableStateOf(config.initialSelectedDates) }
    LaunchedEffect(selectedDate) { clickedNewDate = selectedDate }

    val daysOfWeek = DayOfWeek.entries.rotate(startDayOfWeek.ordinal)
    val eventsByDate = remember(events) { events.groupBy { it.date } }
    val multiSelectDates = when (onDaySelectionAction) {
        is OnDaySelectionAction.Multiple -> clickedNewDates
        else -> emptyList()
    }

    // Pager centre page represents *today's* month so "go to today" is reliable
    val initialMonthOffset = remember(initialMonthStart, todayMonthStart) {
        monthsBetween(todayMonthStart, initialMonthStart)
    }
    val centerPage = Int.MAX_VALUE / 2
    val pagerState = rememberPagerState(
        initialPage = centerPage + initialMonthOffset,
        pageCount = { Int.MAX_VALUE }
    )
    val coroutineScope = rememberCoroutineScope()
    val calendarIconEnabled = pagerState.currentPage != centerPage

    DisposableEffect(controller) {
        val generation = controller?.attachScrollImpl { date ->
            val targetMonthStart = date.minus(date.dayOfMonth - 1, DateTimeUnit.DAY)
            val monthDiff = monthsBetween(todayMonthStart, targetMonthStart)
            pagerState.animateScrollToPage(centerPage + monthDiff)
        }
        onDispose { generation?.let { controller?.detachScrollImpl(it) } }
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
                        pagerState.animateScrollToPage(page = centerPage)
                    }
                }
            }
        )
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val pageMonthStart = todayMonthStart.plus(
                value = page - centerPage,
                unit = DateTimeUnit.MONTH,
            ).let { it.minus(it.dayOfMonth - 1, DateTimeUnit.DAY) }
            val displayDates = getMonthDates(pageMonthStart, startDayOfWeek)

            KalendarScaffold(
                modifier = Modifier.fillMaxWidth(),
                showDayLabel = config.showDayLabel,
                dayOfWeek = { daysOfWeek },
                dayLabelConfig = config.dayLabelConfig,
                dates = { displayDates },
            ) { date ->
                val isCurrentMonth =
                    date.year == pageMonthStart.year && date.month == pageMonthStart.month
                val dateEvents = eventsByDate[date] ?: emptyList()
                val isDisabled = !date.isDateInteractable(config, isPrimaryPeriod = isCurrentMonth)
                val isDateAllowed: (LocalDate) -> Boolean = { candidate ->
                    val primary =
                        candidate.year == pageMonthStart.year &&
                            candidate.month == pageMonthStart.month
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
                                onMultipleClickedNewDate = { d ->
                                    clickedNewDates = clickedNewDates.toMutableList().apply {
                                        if (contains(d)) remove(d) else add(d)
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
    LaunchedEffect(pagerState.currentPage, startDayOfWeek) {
        val pageMonthStart = todayMonthStart.plus(
            value = pagerState.currentPage - centerPage,
            unit = DateTimeUnit.MONTH,
        ).let { it.minus(it.dayOfMonth - 1, DateTimeUnit.DAY) }
        currentMonth = pageMonthStart
        val grid = getMonthDates(pageMonthStart, startDayOfWeek)
        config.onVisibleRangeChange?.invoke(grid.first(), grid.last())
    }
}

internal fun monthsBetween(fromMonthStart: LocalDate, toMonthStart: LocalDate): Int =
    (toMonthStart.year - fromMonthStart.year) * 12 +
        (toMonthStart.monthNumber - fromMonthStart.monthNumber)
