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

package com.himanshoe.kalendar.cosmic

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.himanshoe.kalendar.foundation.action.KalendarDateRange
import com.himanshoe.kalendar.foundation.action.OnDaySelectionAction
import com.himanshoe.kalendar.foundation.event.KalendarEvents
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Represents the current navigation level within the Cosmic calendar.
 *
 * The Cosmic calendar supports three levels of detail that the user can
 * drill into and back out of with animated transitions.
 */
internal sealed interface CosmicLevel {
    /** The year overview showing 12 mini-months. */
    data object Year : CosmicLevel

    /**
     * A full month view drilled into from the year grid.
     *
     * @property month The month being displayed.
     * @property year The year of the month.
     */
    data class MonthDetail(val month: Month, val year: Int) : CosmicLevel

    /**
     * A week view drilled into from the month view.
     *
     * @property weekStartDate The first date of the week being displayed.
     * @property fromMonth The month from which the user drilled down (for back navigation).
     * @property fromYear The year from which the user drilled down.
     */
    data class WeekDetail(
        val weekStartDate: LocalDate,
        val fromMonth: Month,
        val fromYear: Int,
    ) : CosmicLevel
}

/** Returns the depth of the navigation level for animation direction. */
private val CosmicLevel.depth: Int
    get() = when (this) {
        is CosmicLevel.Year -> 0
        is CosmicLevel.MonthDetail -> 1
        is CosmicLevel.WeekDetail -> 2
    }

/**
 * A Compose calendar that displays an entire year as 12 mini-months with
 * configurable drill-down navigation into month and week views.
 *
 * The Cosmic calendar starts with a year overview where all 12 months are shown
 * in a compact 4x3 grid. Users can:
 * - Navigate between years using arrow buttons
 * - Tap a mini-month to drill down into a full month view (configurable)
 * - Tap a week row in the month view to drill into a week view (configurable)
 * - Tap individual days at any level to trigger the [onDaySelectionAction]
 * - Navigate back through levels with animated transitions
 *
 * All drill-down behavior is controlled via [KalendarCosmicKonfig.onMonthClick]
 * and [KalendarCosmicKonfig.onWeekClick]. Set either to [DrillDownMode.Disabled]
 * to prevent that level of navigation.
 *
 * @param modifier Modifier applied to the root layout.
 * @param selectedDate The date to highlight as selected. Defaults to today.
 * @param events Calendar events to display across all views.
 * @param onDaySelectionAction Controls how day selection works (single, multiple, or range).
 * @param cosmicKonfig Configuration for styling, drill-down behavior, and animation timing.
 * @param startDayOfWeek The first day of the week for all views. Defaults to [DayOfWeek.SUNDAY].
 * @param dateRange Optional constraints on which dates are selectable.
 */
@Composable
fun KalendarCosmic(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    events: KalendarEvents = KalendarEvents(),
    onDaySelectionAction: OnDaySelectionAction = OnDaySelectionAction.Single { _, _ -> },
    cosmicKonfig: KalendarCosmicKonfig = KalendarCosmicKonfig(),
    startDayOfWeek: DayOfWeek = DayOfWeek.SUNDAY,
    dateRange: KalendarDateRange = KalendarDateRange(),
) {
    var currentYear by remember { mutableStateOf(selectedDate.year) }
    var currentLevel by remember { mutableStateOf<CosmicLevel>(CosmicLevel.Year) }

    val konfig = cosmicKonfig.kalendarKonfig
    val animDuration = cosmicKonfig.animationDuration

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(brush = Brush.linearGradient(konfig.backgroundColor.value)),
    ) {
        AnimatedContent(
            targetState = currentLevel,
            transitionSpec = {
                val forward = targetState.depth > initialState.depth
                val slideIn = slideInHorizontally(
                    animationSpec = tween(animDuration),
                ) { fullWidth -> if (forward) fullWidth else -fullWidth }
                val slideOut = slideOutHorizontally(
                    animationSpec = tween(animDuration),
                ) { fullWidth -> if (forward) -fullWidth else fullWidth }

                (slideIn + fadeIn(tween(animDuration)))
                    .togetherWith(slideOut + fadeOut(tween(animDuration)))
                    .using(SizeTransform(clip = false))
            },
            label = "CosmicLevelTransition",
        ) { level ->
            when (level) {
                is CosmicLevel.Year -> {
                    CosmicYearGrid(
                        year = currentYear,
                        selectedDate = selectedDate,
                        events = events,
                        dayKonfig = konfig.kalendarDayKonfig,
                        kalendarLocale = konfig.kalendarLocale,
                        startDayOfWeek = startDayOfWeek,
                        dateRange = dateRange,
                        onMonthClick = { month ->
                            if (cosmicKonfig.onMonthClick != DrillDownMode.Disabled) {
                                currentLevel = CosmicLevel.MonthDetail(month, currentYear)
                            }
                        },
                        onDayClick = { date ->
                            // Fire day selection at year level too
                            when (onDaySelectionAction) {
                                is OnDaySelectionAction.Single ->
                                    onDaySelectionAction.onDayClick(date, emptyList())

                                is OnDaySelectionAction.Multiple ->
                                    onDaySelectionAction.onDayClick(date, emptyList())

                                is OnDaySelectionAction.Range -> { /* Range needs two clicks, handled in sub-views */ }
                            }
                        },
                        onPreviousYear = { currentYear-- },
                        onNextYear = { currentYear++ },
                    )
                }

                is CosmicLevel.MonthDetail -> {
                    CosmicMonthView(
                        month = level.month,
                        year = level.year,
                        selectedDate = selectedDate,
                        events = events,
                        onDaySelectionAction = onDaySelectionAction,
                        dayKonfig = konfig.kalendarDayKonfig,
                        kalendarDayLabelKonfig = konfig.kalendarDayLabelKonfig,
                        kalendarLocale = konfig.kalendarLocale,
                        backgroundColor = konfig.backgroundColor,
                        startDayOfWeek = startDayOfWeek,
                        dateRange = dateRange,
                        onBack = { currentLevel = CosmicLevel.Year },
                        onWeekClick = if (cosmicKonfig.onWeekClick != DrillDownMode.Disabled) {
                            { weekStart ->
                                currentLevel = CosmicLevel.WeekDetail(
                                    weekStartDate = weekStart,
                                    fromMonth = level.month,
                                    fromYear = level.year,
                                )
                            }
                        } else {
                            null
                        },
                    )
                }

                is CosmicLevel.WeekDetail -> {
                    CosmicWeekView(
                        weekStartDate = level.weekStartDate,
                        selectedDate = selectedDate,
                        events = events,
                        onDaySelectionAction = onDaySelectionAction,
                        dayKonfig = konfig.kalendarDayKonfig,
                        kalendarDayLabelKonfig = konfig.kalendarDayLabelKonfig,
                        kalendarLocale = konfig.kalendarLocale,
                        backgroundColor = konfig.backgroundColor,
                        startDayOfWeek = startDayOfWeek,
                        dateRange = dateRange,
                        onBack = {
                            currentLevel = CosmicLevel.MonthDetail(
                                month = level.fromMonth,
                                year = level.fromYear,
                            )
                        },
                    )
                }
            }
        }
    }
}
