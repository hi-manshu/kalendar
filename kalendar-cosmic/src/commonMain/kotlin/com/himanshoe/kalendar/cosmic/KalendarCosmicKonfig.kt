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

import com.himanshoe.kalendar.foundation.component.config.KalendarKonfig

/**
 * Configuration for the [KalendarCosmic] composable.
 *
 * Controls the visual styling, drill-down behavior, and animation timing
 * of the annual year view and its sub-views.
 *
 * @property kalendarKonfig The base calendar configuration controlling colors, day styling,
 *   header styling, day labels, and locale. Shared across all drill-down levels.
 * @property onMonthClick Determines what happens when the user taps on a mini-month
 *   in the year grid. Defaults to [DrillDownMode.MonthView] which opens a full month view.
 * @property onWeekClick Determines what happens when the user taps on a week row
 *   inside the drilled-down month view. Defaults to [DrillDownMode.Disabled].
 * @property animationDuration Duration in milliseconds for drill-down and back transitions.
 */
data class KalendarCosmicKonfig(
    val kalendarKonfig: KalendarKonfig = KalendarKonfig(),
    val onMonthClick: DrillDownMode = DrillDownMode.MonthView,
    val onWeekClick: DrillDownMode = DrillDownMode.Disabled,
    val animationDuration: Int = 400,
)
