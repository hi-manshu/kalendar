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

package com.himanshoe.kalendar.foundation.action

import com.himanshoe.kalendar.foundation.event.KalendarEvent
import kotlinx.datetime.LocalDate

/**
 * Defines how day taps are handled by the calendar.
 *
 * Pass one of the subclasses to the `onDaySelectionAction` parameter of [Kalendar].
 */
sealed class OnDaySelectionAction {
    /**
     * Only one day can be selected at a time. Tapping a new day deselects the previous one.
     *
     * @param onDayClick Called with the tapped [LocalDate] and the events on that day.
     */
    data class Single(val onDayClick: (LocalDate, List<KalendarEvent>) -> Unit) :
        OnDaySelectionAction()

    /**
     * Multiple days can be selected independently. Tapping an already-selected day deselects it.
     *
     * @param onDayClick Called with the tapped [LocalDate] and the events on that day.
     */
    data class Multiple(val onDayClick: (LocalDate, List<KalendarEvent>) -> Unit) :
        OnDaySelectionAction()

    /**
     * Two taps define a continuous date range. The first tap sets the start date and the
     * second tap sets the end date. If the second tap is before the first, the dates are
     * automatically swapped so [KalendarSelectedDayRange.start] is always the earlier date.
     *
     * @param onRangeSelected Called with the finalised [KalendarSelectedDayRange] and
     *   the events that fall within the range.
     */
    data class Range(val onRangeSelected: (KalendarSelectedDayRange, List<KalendarEvent>) -> Unit) :
        OnDaySelectionAction()

    companion object {
        /**
         * A no-op action that ignores all day taps. Use this as the default when no
         * tap handling is needed.
         */
        val NoOp: OnDaySelectionAction = Single { _, _ -> }
    }
}
