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

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate

/**
 * Represents a selected date range in the calendar. Implements [ClosedRange] so that the
 * standard `in` operator works naturally:
 *
 * ```kotlin
 * val range = KalendarSelectedDayRange(start, endInclusive)
 * if (someDate in range) { ... }
 * ```
 *
 * @property start The first date in the range (inclusive).
 * @property endInclusive The last date in the range (inclusive). Must be on or after [start].
 * @throws IllegalArgumentException if [endInclusive] is before [start].
 */
@Immutable
data class KalendarSelectedDayRange(
    override val start: LocalDate,
    override val endInclusive: LocalDate,
) : ClosedRange<LocalDate> {
    init {
        require(endInclusive >= start) {
            "endInclusive ($endInclusive) must be on or after start ($start)"
        }
    }
}
