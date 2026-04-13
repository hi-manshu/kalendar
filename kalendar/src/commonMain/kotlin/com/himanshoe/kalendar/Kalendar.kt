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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.himanshoe.kalendar.foundation.action.OnDaySelectionAction
import com.himanshoe.kalendar.foundation.component.config.KalendarConfig
import com.himanshoe.kalendar.foundation.event.KalendarEvents
import com.himanshoe.kalendar.foundation.event.KalendarEvent
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * The main entry point for displaying a calendar.
 *
 * Choose a [KalendarType] to control the layout and navigation style. All visual and
 * behavioural options are consolidated in [KalendarConfig].
 *
 * ```kotlin
 * Kalendar(
 *     type = KalendarType.Oceanic,
 *     events = myEvents,
 *     onDaySelectionAction = OnDaySelectionAction.Single { date, events -> … },
 *     config = KalendarConfig(
 *         startDayOfWeek = DayOfWeek.MONDAY,
 *         minDate = today,
 *         backgroundColor = KalendarColor.Solid(Color.White),
 *     ),
 * )
 * ```
 *
 * @param type The [KalendarType] that controls the layout variant and navigation style.
 * @param modifier [Modifier] applied to the outermost layout container.
 * @param selectedDate The initially selected (and highlighted) date.
 *   Defaults to today in the current system time zone.
 * @param events The full list of [KalendarEvent] instances to display. Each variant
 *   automatically filters this list to the dates visible in the current view.
 * @param onDaySelectionAction Defines how day taps are handled — single selection, multi
 *   selection, or range selection. Defaults to [OnDaySelectionAction.NoOp].
 * @param config Visual and behavioural configuration for the calendar. Controls colours,
 *   arrow visibility, day-label formatting, navigation bounds, and more.
 * @param controller An optional [KalendarController] for programmatic navigation. Obtain one
 *   via [rememberKalendarController] and call [KalendarController.scrollToDate] to navigate
 *   without user interaction.
 * @param dayContent An optional composable slot that completely replaces the built-in day
 *   cell. Receives the [LocalDate], whether it is currently selected, and the events on that
 *   date. Not available for [KalendarType.Agenda].
 */
@Composable
fun Kalendar(
    type: KalendarType,
    modifier: Modifier = Modifier,
    selectedDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    events: KalendarEvents = emptyList(),
    onDaySelectionAction: OnDaySelectionAction = OnDaySelectionAction.NoOp,
    config: KalendarConfig = KalendarConfig(),
    controller: KalendarController? = null,
    dayContent: (@Composable (date: LocalDate, isSelected: Boolean, events: List<KalendarEvent>) -> Unit)? = null,
) {
    when (type) {
        KalendarType.Oceanic -> KalendarOceanic(
            selectedDate = selectedDate,
            modifier = modifier,
            onDaySelectionAction = onDaySelectionAction,
            config = config,
            events = events,
            controller = controller,
            dayContent = dayContent,
        )

        KalendarType.Firey -> KalendarFirey(
            modifier = modifier,
            selectedDate = selectedDate,
            events = events,
            onDaySelectionAction = onDaySelectionAction,
            config = config,
            controller = controller,
            dayContent = dayContent,
        )

        KalendarType.Aerial -> KalendarAerial(
            selectedDate = selectedDate,
            modifier = modifier,
            onDaySelectionAction = onDaySelectionAction,
            config = config,
            events = events,
            controller = controller,
            dayContent = dayContent,
        )

        KalendarType.Solaris -> KalendarSolaris(
            modifier = modifier,
            selectedDate = selectedDate,
            events = events,
            onDaySelectionAction = onDaySelectionAction,
            config = config,
            controller = controller,
            dayContent = dayContent,
        )

        KalendarType.Yearly -> KalendarYearly(
            selectedDate = selectedDate,
            modifier = modifier,
            onDaySelectionAction = onDaySelectionAction,
            config = config,
            events = events,
            controller = controller,
            dayContent = dayContent,
        )

        KalendarType.Agenda -> KalendarAgenda(
            events = events,
            config = config,
            modifier = modifier,
            onDaySelectionAction = onDaySelectionAction,
        )
    }
}
