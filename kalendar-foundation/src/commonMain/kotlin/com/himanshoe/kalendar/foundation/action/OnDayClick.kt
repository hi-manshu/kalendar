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
 * Handles a day tap for [onDaySelectionAction], updating selection state via the provided
 * callbacks.
 *
 * Non-allowed dates (see [isDateAllowed]) are ignored: no state change and no host callback.
 * For [OnDaySelectionAction.Range], completing a range that contains any disallowed day is
 * refused (policy: refuse incomplete booking ranges).
 *
 * @param isDateAllowed Predicate for whether a date may be selected. Defaults to always true
 *   for backward-compatible call sites; production UIs should pass
 *   [LocalDate.isDateInteractable].
 */
fun LocalDate.onDayClick(
    events: List<KalendarEvent>,
    rangeStartDate: LocalDate?,
    rangeEndDate: LocalDate?,
    onDaySelectionAction: OnDaySelectionAction,
    onClickedNewDate: (LocalDate) -> Unit,
    onMultipleClickedNewDate: (LocalDate) -> Unit,
    onClickedRangeStartDate: (LocalDate?) -> Unit,
    onClickedRangeEndDate: (LocalDate?) -> Unit,
    onUpdateSelectedRange: (KalendarSelectedDayRange?) -> Unit,
    allEvents: List<KalendarEvent> = events,
    isDateAllowed: (LocalDate) -> Boolean = { true },
) {
    if (!isDateAllowed(this)) return

    when (onDaySelectionAction) {
        is OnDaySelectionAction.Single -> {
            onClickedNewDate(this)
            onDaySelectionAction.onDayClick(this, events)
        }

        is OnDaySelectionAction.Range -> {
            if (rangeStartDate == null || rangeEndDate != null) {
                onClickedRangeStartDate(this)
                onClickedRangeEndDate(null)
                onUpdateSelectedRange(null)
                onClickedNewDate(this)
            } else {
                var newRangeStartDate = rangeStartDate
                var newRangeEndDate = this
                if (newRangeStartDate > newRangeEndDate) {
                    val temp = newRangeStartDate
                    newRangeStartDate = newRangeEndDate
                    newRangeEndDate = temp
                }
                if (!isRangeFullyAllowed(newRangeStartDate, newRangeEndDate, isDateAllowed)) {
                    return
                }
                onClickedRangeStartDate(newRangeStartDate)
                onClickedRangeEndDate(newRangeEndDate)
                val range = KalendarSelectedDayRange(
                    start = newRangeStartDate,
                    endInclusive = newRangeEndDate,
                )
                onUpdateSelectedRange(range)
                val eventsInRange = allEvents.filter { it.date in range }
                onDaySelectionAction.onRangeSelected(range, eventsInRange)
                onClickedNewDate(this)
            }
        }

        is OnDaySelectionAction.Multiple -> {
            onMultipleClickedNewDate(this)
            onDaySelectionAction.onDayClick(this, events)
        }
    }
}
