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

package com.himanshoe.kalendar.foundation.locale

/**
 * Provides localized strings for day and month names used in the calendar.
 *
 * @property dayNames Full day names ordered Sunday to Saturday (e.g., "Sunday", "Monday"...).
 * @property shortDayNames Short day labels ordered Sunday to Saturday (e.g., "Su", "Mo"...).
 * @property monthNames Full month names ordered January to December.
 */
data class KalendarLocale(
    val dayNames: List<String>,
    val shortDayNames: List<String>,
    val monthNames: List<String>,
) {
    init {
        require(dayNames.size == 7) { "dayNames must have exactly 7 entries" }
        require(shortDayNames.size == 7) { "shortDayNames must have exactly 7 entries" }
        require(monthNames.size == 12) { "monthNames must have exactly 12 entries" }
    }

    companion object {
        fun default(): KalendarLocale = KalendarLocale(
            dayNames = listOf(
                "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
            ),
            shortDayNames = listOf(
                "Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"
            ),
            monthNames = listOf(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            ),
        )
    }
}
