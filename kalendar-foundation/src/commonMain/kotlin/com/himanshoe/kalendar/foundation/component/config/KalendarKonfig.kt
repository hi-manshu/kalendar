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

package com.himanshoe.kalendar.foundation.component.config

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.himanshoe.kalendar.foundation.action.KalendarSelectedDayRange
import com.himanshoe.kalendar.foundation.color.KalendarColor
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

/**
 * Visual and behavioural configuration for [Kalendar].
 *
 * @param showDayLabel Whether to show the row of day-of-week labels (Mon, Tue, …).
 *   Defaults to `true`.
 * @param showArrows Whether to show previous/next navigation arrows. Has no effect on
 *   swipeable variants ([KalendarType.Aerial], [KalendarType.Solaris]) which use a swipe
 *   gesture and a "return to today" icon instead. Defaults to `true`.
 * @param startDayOfWeek The first day of the week column. Defaults to [DayOfWeek.MONDAY].
 * @param firstVisibleDate When set, the calendar initially scrolls to the month or week
 *   containing this date instead of the one containing [selectedDate]. Useful when you want
 *   to open the calendar on a specific page without pre-selecting that date.
 *   Defaults to `null` (falls back to [selectedDate]).
 * @param minDate When set, the user cannot navigate to a month or week that starts before
 *   the month/week containing [minDate]. Defaults to `null` (no lower bound).
 * @param maxDate When set, the user cannot navigate past the month/week containing [maxDate].
 *   Defaults to `null` (no upper bound).
 * @param initialSelectedDates Pre-selected dates used as the initial state for
 *   [OnDaySelectionAction.Multiple][com.himanshoe.kalendar.foundation.action.OnDaySelectionAction.Multiple] mode.
 *   Ignored for other modes. Defaults to an empty list.
 * @param initialSelectedRange Pre-selected date range used as the initial state for
 *   [OnDaySelectionAction.Range][com.himanshoe.kalendar.foundation.action.OnDaySelectionAction.Range] mode.
 *   Ignored for other modes. Defaults to `null`.
 * @param disabledDates A predicate called for each date. When it returns `true` the date is
 *   rendered at reduced opacity and taps are ignored. Defaults to `{ false }`.
 * @param onVisibleRangeChange Called whenever the visible date range changes due to user
 *   navigation or a programmatic scroll. Use this to lazy-load events from a backend.
 *   Defaults to `null`.
 * @param dayConfig Visual configuration for individual day cells.
 * @param headerConfig Visual configuration for the month/week header.
 * @param dayLabelConfig Visual configuration for the day-of-week label row.
 * @param backgroundColor Background color or gradient for the calendar container.
 */
@Immutable
data class KalendarConfig(
    val showDayLabel: Boolean = true,
    val showArrows: Boolean = true,
    val startDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
    val firstVisibleDate: LocalDate? = null,
    val minDate: LocalDate? = null,
    val maxDate: LocalDate? = null,
    val initialSelectedDates: List<LocalDate> = emptyList(),
    val initialSelectedRange: KalendarSelectedDayRange? = null,
    val disabledDates: (LocalDate) -> Boolean = { false },
    val onVisibleRangeChange: ((start: LocalDate, end: LocalDate) -> Unit)? = null,
    val dayConfig: KalendarDayConfig = KalendarDayConfig(),
    val headerConfig: KalendarHeaderConfig = KalendarHeaderConfig.default(),
    val dayLabelConfig: KalendarDayLabelConfig = KalendarDayLabelConfig.default(),
    val backgroundColor: KalendarColor = KalendarColor.Solid(Color.White),
)
