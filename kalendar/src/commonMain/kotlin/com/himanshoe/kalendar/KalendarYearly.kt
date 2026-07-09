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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.himanshoe.kalendar.foundation.action.KalendarSelectedDayRange
import com.himanshoe.kalendar.foundation.action.OnDaySelectionAction
import com.himanshoe.kalendar.foundation.action.onDayClick
import com.himanshoe.kalendar.foundation.component.config.KalendarConfig
import com.himanshoe.kalendar.foundation.component.config.KalendarHeaderConfig
import com.himanshoe.kalendar.foundation.event.KalendarEvents
import com.himanshoe.kalendar.foundation.event.KalendarEvent
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import com.himanshoe.kalendar.foundation.action.isDateInteractable

@Composable
internal fun KalendarYearly(
    selectedDate: LocalDate,
    config: KalendarConfig,
    events: KalendarEvents,
    modifier: Modifier = Modifier,
    controller: KalendarController? = null,
    onDaySelectionAction: OnDaySelectionAction = OnDaySelectionAction.NoOp,
    dayContent: (@Composable (LocalDate, Boolean, List<KalendarEvent>, Boolean) -> Unit)? = null,
) {
    KalendarYearlyContent(
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
private fun KalendarYearlyContent(
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
    val initialDate = config.firstVisibleDate ?: selectedDate
    var currentYear by remember { mutableStateOf(initialDate.year) }
    var clickedDate by remember { mutableStateOf(selectedDate) }
    var clickedNewDates by remember { mutableStateOf(config.initialSelectedDates) }
    val selectedRange = remember {
        mutableStateOf<KalendarSelectedDayRange?>(config.initialSelectedRange)
    }
    var rangeStartDate by remember {
        mutableStateOf<LocalDate?>(config.initialSelectedRange?.start)
    }
    var rangeEndDate by remember {
        mutableStateOf<LocalDate?>(config.initialSelectedRange?.endInclusive)
    }
    val eventsByDate = remember(events) { events.groupBy { it.date } }
    LaunchedEffect(selectedDate) { clickedDate = selectedDate }

    val canGoBack = config.minDate?.let { currentYear > it.year } ?: true
    val canGoForward = config.maxDate?.let { currentYear < it.year } ?: true

    DisposableEffect(controller) {
        val generation = controller?.attachScrollImpl { date ->
            currentYear = date.year
        }
        onDispose { generation?.let { controller?.detachScrollImpl(it) } }
    }

    LaunchedEffect(currentYear) {
        val start = LocalDate(currentYear, Month.JANUARY, 1)
        val end = LocalDate(currentYear, Month.DECEMBER, 31)
        config.onVisibleRangeChange?.invoke(start, end)
    }

    Column(
        modifier = modifier.background(brush = Brush.linearGradient(config.backgroundColor.value)),
    ) {
        YearHeader(
            year = currentYear,
            canGoBack = canGoBack,
            canGoForward = canGoForward,
            headerConfig = config.headerConfig,
            onPreviousClick = { if (canGoBack) currentYear-- },
            onNextClick = { if (canGoForward) currentYear++ },
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Month.entries.forEach { month ->
                MiniMonthGrid(
                    year = currentYear,
                    month = month,
                    today = today,
                    selectedDate = clickedDate,
                    selectedDates = when (onDaySelectionAction) {
                        is OnDaySelectionAction.Multiple -> clickedNewDates
                        else -> emptyList()
                    },
                    selectedRange = selectedRange.value,
                    startDayOfWeek = startDayOfWeek,
                    eventsByDate = eventsByDate,
                    config = config,
                    dayContent = dayContent,
                    onDaySelectionAction = onDaySelectionAction,
                    onDayClick = { date ->
                        val dateEvents = eventsByDate[date] ?: emptyList()
                        date.onDayClick(
                            events = dateEvents,
                            allEvents = events,
                            rangeStartDate = rangeStartDate,
                            rangeEndDate = rangeEndDate,
                            onDaySelectionAction = onDaySelectionAction,
                            onClickedNewDate = { clickedDate = it },
                            onMultipleClickedNewDate = { d ->
                                clickedNewDates = clickedNewDates.toMutableList().apply {
                                    if (contains(d)) remove(d) else add(d)
                                }
                            },
                            onClickedRangeStartDate = { rangeStartDate = it },
                            onClickedRangeEndDate = { rangeEndDate = it },
                            onUpdateSelectedRange = { selectedRange.value = it },
                            isDateAllowed = { it.isDateInteractable(config) },
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun YearHeader(
    year: Int,
    canGoBack: Boolean,
    canGoForward: Boolean,
    headerConfig: KalendarHeaderConfig,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onPreviousClick, enabled = canGoBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous year",
            )
        }
        Text(
            text = year.toString(),
            style = headerConfig.textStyle,
            modifier = Modifier.wrapContentSize(),
        )
        IconButton(onClick = onNextClick, enabled = canGoForward) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next year",
            )
        }
    }
}

@Composable
private fun MiniMonthGrid(
    year: Int,
    month: Month,
    today: LocalDate,
    selectedDate: LocalDate,
    selectedDates: List<LocalDate>,
    selectedRange: KalendarSelectedDayRange?,
    startDayOfWeek: DayOfWeek,
    eventsByDate: Map<LocalDate, List<KalendarEvent>>,
    config: KalendarConfig,
    onDaySelectionAction: OnDaySelectionAction,
    onDayClick: (LocalDate) -> Unit,
    dayContent: (@Composable (LocalDate, Boolean, List<KalendarEvent>, Boolean) -> Unit)?,
) {
    val firstOfMonth = LocalDate(year, month, 1)
    val dates = getMonthDates(firstOfMonth, startDayOfWeek)
    val daysOfWeek = DayOfWeek.entries.rotate(startDayOfWeek.ordinal)
    val monthName = month.name
        .lowercase()
        .replaceFirstChar { it.uppercaseChar() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
    ) {
        Text(
            text = monthName,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                brush = Brush.linearGradient(
                    listOf(Color(0xFF413D4B), Color(0xFFD8A29E))
                ),
            ),
            modifier = Modifier.padding(bottom = 4.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            daysOfWeek.forEach { day ->
                val label = config.dayLabelConfig.dayNameFormatter?.invoke(day)
                    ?: day.name.take(1)
                Text(
                    text = label,
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF613D4B),
                    ),
                    modifier = Modifier.weight(1f),
                )
            }
        }
        dates.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                week.forEach { date ->
                    Box(modifier = Modifier.weight(1f)) {
                        if (date.month == month && date.year == year) {
                            val isSelected = when (onDaySelectionAction) {
                                is OnDaySelectionAction.Multiple -> date in selectedDates
                                is OnDaySelectionAction.Range ->
                                    selectedRange?.let { date in it } == true || date == selectedDate
                                else -> date == selectedDate
                            }
                            val isToday = date == today
                            val isDisabled = !date.isDateInteractable(config, isPrimaryPeriod = true)
                            val dateEvents = eventsByDate[date] ?: emptyList()

                            if (dayContent != null) {
                                dayContent(date, isSelected, dateEvents, isDisabled)
                            } else {
                                MiniDayCell(
                                    date = date,
                                    isSelected = isSelected,
                                    isToday = isToday,
                                    isDisabled = isDisabled,
                                    hasEvents = dateEvents.isNotEmpty(),
                                    onClick = { onDayClick(date) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniDayCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    isDisabled: Boolean,
    hasEvents: Boolean,
    onClick: () -> Unit,
) {
    val background = when {
        isSelected -> Brush.linearGradient(listOf(Color(0xFFF7CFD3), Color(0xFFF7CFD3)))
        else -> Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
    }
    val textColor = when {
        isSelected -> Color(0xFF413D4B)
        isToday -> Color(0xFFD8A29E)
        else -> Color(0xFF413D4B)
    }
    val fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .aspectRatio(1f)
            .alpha(if (isDisabled) 0.38f else 1f)
            .padding(1.dp)
            .clip(CircleShape)
            .background(brush = background)
            .then(if (!isDisabled) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = fontWeight,
                    textAlign = TextAlign.Center,
                    color = textColor,
                ),
            )
            if (hasEvents) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFFD8A29E))
                        .padding(2.dp),
                )
            }
        }
    }
}
