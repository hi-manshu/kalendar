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

package com.himanshoe.kalendar.foundation.action

import com.himanshoe.kalendar.foundation.event.KalenderEvent
import kotlinx.datetime.LocalDate

/**
 * Defines the day selection mode for the calendar.
 *
 * Determines how user taps on day cells are interpreted and what callback is invoked.
 */
sealed class OnDaySelectionAction {

    /**
     * Only one day can be selected at a time. Tapping a new day deselects the previous one.
     *
     * @property onDayClick Called with the tapped date and its associated events.
     */
    data class Single(val onDayClick: (LocalDate, List<KalenderEvent>) -> Unit) :
        OnDaySelectionAction()

    /**
     * Multiple days can be selected simultaneously. Tapping a selected day deselects it.
     *
     * @property onDayClick Called with the tapped date and its associated events.
     */
    data class Multiple(val onDayClick: (LocalDate, List<KalenderEvent>) -> Unit) :
        OnDaySelectionAction()

    /**
     * Two taps define a date range. The first tap sets the start, the second sets the end.
     *
     * @property onRangeSelected Called with the selected range and events within it.
     */
    data class Range(val onRangeSelected: (KalendarSelectedDayRange, List<KalenderEvent>) -> Unit) :
        OnDaySelectionAction()
}
