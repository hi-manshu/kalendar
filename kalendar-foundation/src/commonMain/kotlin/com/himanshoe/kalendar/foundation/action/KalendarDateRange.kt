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

import kotlinx.datetime.LocalDate

/**
 * Defines the selectable date range for the calendar.
 *
 * @property minDate The earliest selectable date. Null means no lower bound.
 * @property maxDate The latest selectable date. Null means no upper bound.
 * @property disabledDates Specific dates that should be disabled regardless of min/max range.
 */
data class KalendarDateRange(
    val minDate: LocalDate? = null,
    val maxDate: LocalDate? = null,
    val disabledDates: Set<LocalDate> = emptySet(),
) {
    /**
     * Checks whether the given date is enabled (selectable) within this range.
     */
    fun isDateEnabled(date: LocalDate): Boolean {
        if (disabledDates.contains(date)) return false
        if (minDate != null && date < minDate) return false
        if (maxDate != null && date > maxDate) return false
        return true
    }
}
