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
import com.himanshoe.kalendar.foundation.action.isDateInteractable

@Composable
internal fun KalendarAerial(
    selectedDate: LocalDate,
    modifier: Modifier = Modifier,
    events: KalendarEvents = emptyList(),
    onDaySelectionAction: OnDaySelectionAction = OnDaySelectionAction.NoOp,
    config: KalendarConfig = KalendarConfig(),
    controller: KalendarController? = null,
    dayContent: (@Composable (LocalDate, Boolean, List<KalendarEvent>, Boolean) -> Unit)? = null,
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
    dayContent: (@Composable (LocalDate, Boolean, List<KalendarEvent>, Boolean) -> Unit)? = null,
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
    var clickedNewDates by remember { mutableStateOf(config.initialSelectedDates) }
    LaunchedEffect(selectedDate) { clickedNewDate = selectedDate }

    val daysOfWeek = DayOfWeek.entries.rotate(distance = startDayOfWeek.ordinal)
    val multiSelectDates = when (onDaySelectionAction) {
        is OnDaySelectionAction.Multiple -> clickedNewDates
        else -> emptyList()
    }
    // Pager centre = week containing *today*
    val centerPage = Int.MAX_VALUE / 2
    val initialPageOffset = remember(initialDate, today, startDayOfWeek) {
        weeksBetweenAligned(today, initialDate, startDayOfWeek)
    }
    val pagerState = rememberPagerState(
        initialPage = centerPage + initialPageOffset,
        pageCount = { Int.MAX_VALUE }
    )
    val eventsByDate = remember(events) { events.groupBy { it.date } }
    val calendarIconEnabled = pagerState.currentPage != centerPage
    val headerText = remember(currentDay, startDayOfWeek) {
        getWeekDates(currentDay = currentDay, startDayOfWeek = startDayOfWeek).buildHeaderText()
    }

    DisposableEffect(controller) {
        val generation = controller?.attachScrollImpl { date ->
            val weekOffset = weeksBetweenAligned(today, date, startDayOfWeek)
            pagerState.animateScrollToPage(centerPage + weekOffset)
        }
        onDispose { generation?.let { controller?.detachScrollImpl(it) } }
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
                        pagerState.animateScrollToPage(page = centerPage)
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
            val startDate = today.plus(
                value = (page - centerPage) * 7,
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

        LaunchedEffect(pagerState.currentPage) {
            val startDate = today.plus(
                value = (pagerState.currentPage - centerPage) * 7,
                unit = DateTimeUnit.DAY
            )
            currentDay = startDate
            val weekDates = getWeekDates(currentDay, startDayOfWeek)
            config.onVisibleRangeChange?.invoke(weekDates.first(), weekDates.last())
        }
    }
}

/**
 * Whole-week distance from the week containing [from] to the week containing [to],
 * using floor division so negative offsets align correctly.
 */
internal fun weeksBetweenAligned(
    from: LocalDate,
    to: LocalDate,
    startDayOfWeek: DayOfWeek,
): Int {
    val fromStart = getWeekDates(from, startDayOfWeek).first()
    val toStart = getWeekDates(to, startDayOfWeek).first()
    val dayDiff = toStart.toEpochDays() - fromStart.toEpochDays()
    return (dayDiff / 7).toInt()
}
