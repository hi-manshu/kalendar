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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.himanshoe.kalendar.cosmic.KalendarCosmic
import com.himanshoe.kalendar.foundation.action.KalendarDateRange
import com.himanshoe.kalendar.foundation.action.OnDaySelectionAction
import com.himanshoe.kalendar.foundation.component.config.KalendarKonfig
import com.himanshoe.kalendar.foundation.event.KalendarEvents
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * The main entry point for displaying a Kalendar calendar.
 *
 * Delegates to the appropriate calendar implementation based on the supplied [kalendarType].
 * All calendar types share the same parameter set for consistent API usage.
 *
 * @param kalendarType The layout style to render. See [KalendarType] for options.
 * @param modifier Modifier applied to the calendar root layout.
 * @param selectedDate The date to highlight as selected. Defaults to today.
 * @param events A collection of [KalendarEvents] to display as indicators on day cells.
 * @param showDayLabel Whether to show the day-of-week label row (e.g., Mo, Tu, We…).
 * @param arrowShown Whether to show navigation arrows (applicable to [KalendarType.Firey] and [KalendarType.Oceanic]).
 * @param onDaySelectionAction Controls day selection behaviour: single, multiple, or range.
 * @param kalendarKonfig Visual configuration for day cells, headers, labels, background, and locale.
 * @param restrictToCurrentWeekOrMonth When true, prevents navigating before the current week/month.
 * @param startDayOfWeek The first day of each week column. Defaults to [DayOfWeek.SUNDAY].
 * @param dateRange Optional constraints on which dates are selectable.
 */
@Composable
fun Kalendar(
    kalendarType: KalendarType,
    modifier: Modifier = Modifier,
    selectedDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    events: KalendarEvents = KalendarEvents(),
    showDayLabel: Boolean = true,
    arrowShown: Boolean = true,
    onDaySelectionAction: OnDaySelectionAction = OnDaySelectionAction.Single { _, _ -> },
    kalendarKonfig: KalendarKonfig = KalendarKonfig(),
    restrictToCurrentWeekOrMonth: Boolean = false,
    startDayOfWeek: DayOfWeek = DayOfWeek.SUNDAY,
    dateRange: KalendarDateRange = KalendarDateRange(),
) {
    when (kalendarType) {
        KalendarType.Oceanic -> {
            KalendarOceanic(
                selectedDate = selectedDate,
                modifier = modifier,
                arrowShown = arrowShown,
                showDayLabel = showDayLabel,
                onDaySelectionAction = onDaySelectionAction,
                kalendarKonfig = kalendarKonfig,
                events = events,
                startDayOfWeek = startDayOfWeek,
                restrictToCurrentMonth = restrictToCurrentWeekOrMonth,
                dateRange = dateRange,
            )
        }

        KalendarType.Firey -> {
            KalendarFirey(
                modifier = modifier,
                selectedDate = selectedDate,
                events = events,
                startDayOfWeek = startDayOfWeek,
                showDayLabel = showDayLabel,
                arrowShown = arrowShown,
                onDaySelectionAction = onDaySelectionAction,
                kalendarKonfig = kalendarKonfig,
                restrictToCurrentWeek = restrictToCurrentWeekOrMonth,
                dateRange = dateRange,
            )
        }

        KalendarType.Aerial -> {
            KalendarAerial(
                selectedDate = selectedDate,
                modifier = modifier,
                showDayLabel = showDayLabel,
                onDaySelectionAction = onDaySelectionAction,
                kalendarKonfig = kalendarKonfig,
                events = events,
                startDayOfWeek = startDayOfWeek,
                dateRange = dateRange,
            )
        }

        KalendarType.Solaris -> {
            KalendarSolaris(
                modifier = modifier,
                selectedDate = selectedDate,
                events = events,
                startDayOfWeek = startDayOfWeek,
                showDayLabel = showDayLabel,
                onDaySelectionAction = onDaySelectionAction,
                kalendarKonfig = kalendarKonfig,
                dateRange = dateRange,
            )
        }

        KalendarType.Cosmic -> {
            KalendarCosmic(
                modifier = modifier,
                selectedDate = selectedDate,
                events = events,
                onDaySelectionAction = onDaySelectionAction,
                startDayOfWeek = startDayOfWeek,
                dateRange = dateRange,
            )
        }
    }
}
