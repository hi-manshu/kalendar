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
                    endInclusive = newRangeEndDate,
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
