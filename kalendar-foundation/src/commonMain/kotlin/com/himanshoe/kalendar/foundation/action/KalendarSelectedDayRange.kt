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
 * Represents a contiguous range of selected dates in the calendar.
 *
 * Used by [OnDaySelectionAction.Range] to track the user's range selection.
 *
 * @property start The first date in the range (inclusive).
 * @property end The last date in the range (inclusive). Always >= [start].
 */
data class KalendarSelectedDayRange(
    val start: LocalDate,
    val end: LocalDate,
)
