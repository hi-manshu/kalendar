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

import com.himanshoe.kalendar.foundation.component.config.KalendarConfig
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/**
 * Returns whether [this] date may be interacted with (tapped / included in a range).
 *
 * A date is not interactable when:
 * - [isPrimaryPeriod] is `false` (e.g. padding day from an adjacent month),
 * - [KalendarConfig.disabledDates] returns `true`,
 * - it is strictly before [KalendarConfig.minDate], or
 * - it is strictly after [KalendarConfig.maxDate].
 *
 * @param config Calendar configuration supplying disable predicate and bounds.
 * @param isPrimaryPeriod When `false`, the date is treated as non-interactable padding.
 */
fun LocalDate.isDateInteractable(
    config: KalendarConfig,
    isPrimaryPeriod: Boolean = true,
): Boolean {
    if (!isPrimaryPeriod) return false
    if (config.disabledDates(this)) return false
    val minDate = config.minDate
    val maxDate = config.maxDate
    val beforeMin = minDate != null && this < minDate
    val afterMax = maxDate != null && this > maxDate
    return !beforeMin && !afterMax
}

/**
 * Returns `true` when every calendar day in the closed range
 * [[start], [endInclusive]] satisfies [isDateAllowed].
 */
fun isRangeFullyAllowed(
    start: LocalDate,
    endInclusive: LocalDate,
    isDateAllowed: (LocalDate) -> Boolean,
): Boolean {
    val rangeStart = minOf(start, endInclusive)
    val rangeEnd = maxOf(start, endInclusive)
    var cursor = rangeStart
    while (cursor <= rangeEnd) {
        if (!isDateAllowed(cursor)) return false
        cursor = cursor.plus(1, DateTimeUnit.DAY)
    }
    return true
}
