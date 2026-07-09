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
import com.himanshoe.kalendar.foundation.event.KalendarEvent
import com.himanshoe.kalendar.foundation.event.KalendarEvents
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import com.himanshoe.kalendar.foundation.action.isDateInteractable

/**
 * Meteorological seasons for the northern hemisphere.
 *
 * | Season | Months |
 * |---|---|
 * | [Spring] | March – May |
 * | [Summer] | June – August |
 * | [Autumn] | September – November |
 * | [Winter] | December – February (crosses year boundary) |
 */
enum class KalendarSeason {
    Spring,
    Summer,
    Autumn,
    Winter,
}

/**
 * A season period anchored by the year of its first month and the [KalendarSeason] itself.
 *
 * Winter of year `Y` covers December of `Y` plus January and February of `Y + 1`.
 */
data class KalendarSeasonPeriod(
    val year: Int,
    val season: KalendarSeason,
) {
    /**
     * The three [Month] values that belong to this season, in chronological order.
     * For [KalendarSeason.Winter] the list is December, January, February.
     */
    val months: List<Month>
        get() = when (season) {
            KalendarSeason.Spring -> listOf(Month.MARCH, Month.APRIL, Month.MAY)
            KalendarSeason.Summer -> listOf(Month.JUNE, Month.JULY, Month.AUGUST)
            KalendarSeason.Autumn -> listOf(Month.SEPTEMBER, Month.OCTOBER, Month.NOVEMBER)
            KalendarSeason.Winter -> listOf(Month.DECEMBER, Month.JANUARY, Month.FEBRUARY)
        }

    /**
     * Calendar year of each month in [months], parallel to that list.
     * Winter spans [year] for December and [year] + 1 for January/February.
     */
    val years: List<Int>
        get() = when (season) {
            KalendarSeason.Winter -> listOf(year, year + 1, year + 1)
            else -> listOf(year, year, year)
        }

    /**
     * First day of the first month of this season.
     */
    val startDate: LocalDate
        get() = LocalDate(years.first(), months.first(), 1)

    /**
     * Last day of the last month of this season.
     */
    val endDate: LocalDate
        get() {
            val lastYear = years.last()
            val lastMonth = months.last()
            return LocalDate(lastYear, lastMonth, daysInMonth(lastYear, lastMonth))
        }

    /**
     * Human-readable title such as `"Spring 2026"` or `"Winter 2026–2027"`.
     */
    fun title(): String = when (season) {
        KalendarSeason.Winter -> "Winter $year–${year + 1}"
        else -> "${season.name} $year"
    }

    /**
     * Previous meteorological season.
     */
    fun previous(): KalendarSeasonPeriod = when (season) {
        KalendarSeason.Spring -> KalendarSeasonPeriod(year - 1, KalendarSeason.Winter)
        KalendarSeason.Summer -> KalendarSeasonPeriod(year, KalendarSeason.Spring)
        KalendarSeason.Autumn -> KalendarSeasonPeriod(year, KalendarSeason.Summer)
        KalendarSeason.Winter -> KalendarSeasonPeriod(year, KalendarSeason.Autumn)
    }

    /**
     * Next meteorological season.
     */
    fun next(): KalendarSeasonPeriod = when (season) {
        KalendarSeason.Spring -> KalendarSeasonPeriod(year, KalendarSeason.Summer)
        KalendarSeason.Summer -> KalendarSeasonPeriod(year, KalendarSeason.Autumn)
        KalendarSeason.Autumn -> KalendarSeasonPeriod(year, KalendarSeason.Winter)
        KalendarSeason.Winter -> KalendarSeasonPeriod(year + 1, KalendarSeason.Spring)
    }
}

/**
 * Accent palette applied to month headers and selection chrome for a given season.
 */
data class KalendarSeasonPalette(
    val primary: Color,
    val secondary: Color,
    val selectedBackground: Color,
    val label: Color,
)

/**
 * Default palettes tuned to each meteorological season.
 */
fun KalendarSeason.defaultPalette(): KalendarSeasonPalette = when (this) {
    KalendarSeason.Spring -> KalendarSeasonPalette(
        primary = Color(0xFF2D6A4F),
        secondary = Color(0xFF95D5B2),
        selectedBackground = Color(0xFFD8F3DC),
        label = Color(0xFF40916C),
    )
    KalendarSeason.Summer -> KalendarSeasonPalette(
        primary = Color(0xFFB45309),
        secondary = Color(0xFFFBBF24),
        selectedBackground = Color(0xFFFEF3C7),
        label = Color(0xFFD97706),
    )
    KalendarSeason.Autumn -> KalendarSeasonPalette(
        primary = Color(0xFF9A3412),
        secondary = Color(0xFFFB923C),
        selectedBackground = Color(0xFFFED7AA),
        label = Color(0xFFC2410C),
    )
    KalendarSeason.Winter -> KalendarSeasonPalette(
        primary = Color(0xFF1E3A5F),
        secondary = Color(0xFF93C5FD),
        selectedBackground = Color(0xFFDBEAFE),
        label = Color(0xFF3B82F6),
    )
}

/**
 * Resolves the [KalendarSeasonPeriod] that contains [date] using northern-hemisphere
 * meteorological seasons.
 */
fun seasonPeriodOf(date: LocalDate): KalendarSeasonPeriod = when (date.month) {
    Month.MARCH, Month.APRIL, Month.MAY ->
        KalendarSeasonPeriod(date.year, KalendarSeason.Spring)
    Month.JUNE, Month.JULY, Month.AUGUST ->
        KalendarSeasonPeriod(date.year, KalendarSeason.Summer)
    Month.SEPTEMBER, Month.OCTOBER, Month.NOVEMBER ->
        KalendarSeasonPeriod(date.year, KalendarSeason.Autumn)
    Month.DECEMBER ->
        KalendarSeasonPeriod(date.year, KalendarSeason.Winter)
    Month.JANUARY, Month.FEBRUARY ->
        KalendarSeasonPeriod(date.year - 1, KalendarSeason.Winter)
}

/**
 * Number of whole seasons between [from] and [to] (positive when [to] is later).
 */
internal fun seasonsBetween(from: KalendarSeasonPeriod, to: KalendarSeasonPeriod): Int {
    val fromIndex = from.year * 4 + from.season.ordinal
    val toIndex = to.year * 4 + to.season.ordinal
    return toIndex - fromIndex
}

internal fun daysInMonth(year: Int, month: Month): Int = when (month) {
    Month.JANUARY, Month.MARCH, Month.MAY, Month.JULY,
    Month.AUGUST, Month.OCTOBER, Month.DECEMBER -> 31
    Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
    Month.FEBRUARY -> if (isLeapYear(year)) 29 else 28
}

internal fun isLeapYear(year: Int): Boolean =
    (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

@Composable
internal fun KalendarSeason(
    selectedDate: LocalDate,
    config: KalendarConfig,
    events: KalendarEvents,
    modifier: Modifier = Modifier,
    controller: KalendarController? = null,
    onDaySelectionAction: OnDaySelectionAction = OnDaySelectionAction.NoOp,
    dayContent: (@Composable (LocalDate, Boolean, List<KalendarEvent>, Boolean) -> Unit)? = null,
) {
    KalendarSeasonContent(
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
private fun KalendarSeasonContent(
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
    var currentPeriod by remember { mutableStateOf(seasonPeriodOf(initialDate)) }
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

    val palette = remember(currentPeriod.season) { currentPeriod.season.defaultPalette() }

    val canGoBack = config.minDate?.let { min ->
        seasonsBetween(seasonPeriodOf(min), currentPeriod) > 0
    } ?: true
    val canGoForward = config.maxDate?.let { max ->
        seasonsBetween(currentPeriod, seasonPeriodOf(max)) > 0
    } ?: true

    DisposableEffect(controller) {
        val generation = controller?.attachScrollImpl { date ->
            currentPeriod = seasonPeriodOf(date)
        }
        onDispose { generation?.let { controller?.detachScrollImpl(it) } }
    }

    LaunchedEffect(currentPeriod) {
        config.onVisibleRangeChange?.invoke(currentPeriod.startDate, currentPeriod.endDate)
    }

    Column(
        modifier = modifier.background(brush = Brush.linearGradient(config.backgroundColor.value)),
    ) {
        SeasonHeader(
            title = currentPeriod.title(),
            canGoBack = canGoBack,
            canGoForward = canGoForward,
            headerConfig = config.headerConfig,
            accent = palette.primary,
            onPreviousClick = { if (canGoBack) currentPeriod = currentPeriod.previous() },
            onNextClick = { if (canGoForward) currentPeriod = currentPeriod.next() },
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            currentPeriod.months.forEachIndexed { index, month ->
                val monthYear = currentPeriod.years[index]
                SeasonMiniMonthGrid(
                    year = monthYear,
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
                    palette = palette,
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
private fun SeasonHeader(
    title: String,
    canGoBack: Boolean,
    canGoForward: Boolean,
    headerConfig: KalendarHeaderConfig,
    accent: Color,
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
                contentDescription = "Previous season",
                tint = accent,
            )
        }
        Text(
            text = title,
            style = TextStyle(
                color = accent,
                fontSize = headerConfig.textStyle.fontSize,
                fontWeight = headerConfig.textStyle.fontWeight,
                textAlign = headerConfig.textStyle.textAlign,
            ),
            modifier = Modifier.wrapContentSize(),
        )
        IconButton(onClick = onNextClick, enabled = canGoForward) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next season",
                tint = accent,
            )
        }
    }
}

@Composable
private fun SeasonMiniMonthGrid(
    year: Int,
    month: Month,
    today: LocalDate,
    selectedDate: LocalDate,
    selectedDates: List<LocalDate>,
    selectedRange: KalendarSelectedDayRange?,
    startDayOfWeek: DayOfWeek,
    eventsByDate: Map<LocalDate, List<KalendarEvent>>,
    config: KalendarConfig,
    palette: KalendarSeasonPalette,
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
            text = "$monthName $year",
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                brush = Brush.linearGradient(
                    listOf(palette.primary, palette.secondary)
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
                        color = palette.label,
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
                                SeasonMiniDayCell(
                                    date = date,
                                    isSelected = isSelected,
                                    isToday = isToday,
                                    isDisabled = isDisabled,
                                    hasEvents = dateEvents.isNotEmpty(),
                                    palette = palette,
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
private fun SeasonMiniDayCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    isDisabled: Boolean,
    hasEvents: Boolean,
    palette: KalendarSeasonPalette,
    onClick: () -> Unit,
) {
    val background = when {
        isSelected -> Brush.linearGradient(
            listOf(palette.selectedBackground, palette.selectedBackground)
        )
        else -> Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
    }
    val textColor = when {
        isSelected -> palette.primary
        isToday -> palette.secondary
        else -> palette.primary
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
                        .background(palette.secondary)
                        .padding(2.dp),
                )
            }
        }
    }
}
