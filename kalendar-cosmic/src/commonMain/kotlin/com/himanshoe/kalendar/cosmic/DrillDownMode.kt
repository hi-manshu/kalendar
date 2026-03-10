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

package com.himanshoe.kalendar.cosmic

/**
 * Defines the drill-down behavior when the user interacts with the Cosmic calendar.
 *
 * Controls what happens when a user taps on a month or week in the year overview,
 * allowing configurable navigation depth within the [KalendarCosmic] composable.
 */
sealed interface DrillDownMode {

    /**
     * No drill-down navigation occurs. Tapping a month or week has no effect
     * beyond the default day selection behavior.
     */
    data object Disabled : DrillDownMode

    /**
     * Drilling down opens a full month view, similar to [KalendarOceanic][com.himanshoe.kalendar.KalendarOceanic].
     */
    data object MonthView : DrillDownMode

    /**
     * Drilling down opens a week view, similar to [KalendarFirey][com.himanshoe.kalendar.KalendarFirey].
     */
    data object WeekView : DrillDownMode
}
