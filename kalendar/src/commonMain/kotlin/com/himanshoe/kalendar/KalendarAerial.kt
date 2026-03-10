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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
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
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

@Composable
internal fun KalendarAerial(
    selectedDate: LocalDate,
    modifier: Modifier = Modifier,
    events: KalendarEvents = KalendarEvents(),
    showDayLabel: Boolean = true,
    onDaySelectionAction: OnDaySelectionAction = OnDaySelectionAction.Single { _, _ -> },
    kalendarKonfig: KalendarKonfig = KalendarKonfig(),
    restrictToCurrentWeek: Boolean = false,
    startDayOfWeek: DayOfWeek = DayOfWeek.SUNDAY,
    dateRange: KalendarDateRange = KalendarDateRange(),
) {
    KalendarAerialContent(
        selectedDate = selectedDate,
        modifier = modifier,
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
private fun KalendarAerialContent(
    selectedDate: LocalDate,
    modifier: Modifier,
    showDayLabel: Boolean,
    onDaySelectionAction: OnDaySelectionAction,
    dayKonfig: KalendarDayKonfig,
    kalendarHeaderKonfig: KalendarHeaderKonfig,
    kalendarDayLabelKonfig: KalendarDayLabelKonfig,
    restrictToCurrentWeek: Boolean,
    events: KalendarEvents,
    backgroundColor: KalendarColor,
    startDayOfWeek: DayOfWeek,
    dateRange: KalendarDateRange = KalendarDateRange(),
    kalendarLocale: KalendarLocale = KalendarLocale.default(),
) {
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }

    var currentDay by remember { mutableStateOf(selectedDate) }
    var rangeStartDate by remember { mutableStateOf<LocalDate?>(null) }
    var rangeEndDate by remember { mutableStateOf<LocalDate?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val selectedRange = remember { mutableStateOf<KalendarSelectedDayRange?>(null) }
    var clickedNewDate by remember { mutableStateOf(selectedDate) }
    var clickedNewDates by remember { mutableStateOf(listOf(selectedDate)) }
    val daysOfWeek = DayOfWeek.entries.rotate(distance = startDayOfWeek.ordinal)
    val pagerState = rememberPagerState(
        initialPage = Int.MAX_VALUE / 2,
        pageCount = { Int.MAX_VALUE }
    )
    val calendarIconEnabled = pagerState.currentPage != Int.MAX_VALUE / 2
    val headerText = remember(currentDay) {
        getWeekDates(
            currentDay = currentDay,
            startDayOfWeek = startDayOfWeek
        ).buildHeaderText(kalendarLocale)
    }

    Column(
        modifier = modifier.background(brush = Brush.linearGradient(backgroundColor.value)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        KalendarHeader(
            modifier = Modifier,
            title = headerText,
            arrowShown = false,
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
            kalendarHeaderKonfig = kalendarHeaderKonfig,
            canNavigateBack = !restrictToCurrentWeek || currentDay > today,
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val startDate = selectedDate.plus(
                value = (page - Int.MAX_VALUE / 2) * 7,
                unit = DateTimeUnit.DAY
            )
            val displayDates = getWeekDates(
                currentDay = startDate,
                startDayOfWeek = startDayOfWeek
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

        LaunchedEffect(pagerState.currentPage) {
            val startDate = selectedDate.plus(
                value = (pagerState.currentPage - Int.MAX_VALUE / 2) * 7,
                unit = DateTimeUnit.DAY
            )
            currentDay = startDate
        }
    }
}
