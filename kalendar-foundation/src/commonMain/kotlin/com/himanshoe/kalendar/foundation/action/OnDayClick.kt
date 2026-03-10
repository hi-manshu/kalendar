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
 * Handles a day click by dispatching to the appropriate [OnDaySelectionAction] handler.
 *
 * This extension function centralises the selection logic for single, multiple, and range
 * day selection modes, updating the relevant state callbacks accordingly.
 *
 * @param events The events associated with this date.
 * @param rangeStartDate The current range start date (for range selection mode).
 * @param rangeEndDate The current range end date (for range selection mode).
 * @param onDaySelectionAction The active selection mode and its callback.
 * @param onClickedNewDate Called to update the single selected date.
 * @param onMultipleClickedNewDate Called to toggle a date in the multi-selection list.
 * @param onClickedRangeStartDate Called to update the range start date.
 * @param onClickedRangeEndDate Called to update the range end date.
 * @param onUpdateSelectedRange Called to update the full selected range.
 */
fun LocalDate.onDayClick(
    events: List<KalenderEvent>,
    rangeStartDate: LocalDate?,
    rangeEndDate: LocalDate?,
    onDaySelectionAction: OnDaySelectionAction,
    onClickedNewDate: (LocalDate) -> Unit,
    onMultipleClickedNewDate: (LocalDate) -> Unit,
    onClickedRangeStartDate: (LocalDate?) -> Unit,
    onClickedRangeEndDate: (LocalDate?) -> Unit,
    onUpdateSelectedRange: (KalendarSelectedDayRange?) -> Unit,
) {
    when (onDaySelectionAction) {
        is OnDaySelectionAction.Single -> {
            onClickedNewDate(this)
            onDaySelectionAction.onDayClick(this, events)
        }

        is OnDaySelectionAction.Range -> {
            if (rangeStartDate == null || rangeEndDate != null) {
                onClickedRangeStartDate(this)
                onClickedRangeEndDate(null)
            } else {
                var newRangeStartDate = rangeStartDate
                var newRangeEndDate = this
                if (newRangeStartDate > newRangeEndDate) {
                    val temp = newRangeStartDate
                    newRangeStartDate = newRangeEndDate
                    newRangeEndDate = temp
                }
                onClickedRangeStartDate(newRangeStartDate)
                onClickedRangeEndDate(newRangeEndDate)
                val range = KalendarSelectedDayRange(
                    start = newRangeStartDate,
                    end = newRangeEndDate
                )
                onUpdateSelectedRange(range)
                onDaySelectionAction.onRangeSelected(range, events)
            }
            onClickedNewDate(this)
        }

        is OnDaySelectionAction.Multiple -> {
            onMultipleClickedNewDate(this)
            onDaySelectionAction.onDayClick(this, events)
        }
    }
}
