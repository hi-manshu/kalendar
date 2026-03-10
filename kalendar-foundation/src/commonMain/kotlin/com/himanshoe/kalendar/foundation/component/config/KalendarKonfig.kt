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

package com.himanshoe.kalendar.foundation.component.config

import androidx.compose.ui.graphics.Color
import com.himanshoe.kalendar.foundation.color.KalendarColor
import com.himanshoe.kalendar.foundation.locale.KalendarLocale

/**
 * Top-level configuration for the Kalendar composable.
 *
 * Aggregates all sub-configurations that control the visual appearance
 * and locale of the calendar.
 *
 * @property kalendarDayKonfig Configuration for individual day cells (size, colours, text).
 * @property kalendarHeaderKonfig Configuration for the calendar header (text style, alignment).
 * @property kalendarDayLabelKonfig Configuration for the day-of-week label row.
 * @property backgroundColor Background colour or gradient applied to the calendar container.
 * @property kalendarLocale Locale providing localised day and month name strings.
 */
data class KalendarKonfig(
    val kalendarDayKonfig: KalendarDayKonfig = KalendarDayKonfig.default(),
    val kalendarHeaderKonfig: KalendarHeaderKonfig = KalendarHeaderKonfig.default(),
    val kalendarDayLabelKonfig: KalendarDayLabelKonfig = KalendarDayLabelKonfig.default(),
    val backgroundColor: KalendarColor = KalendarColor.Solid(Color.White),
    val kalendarLocale: KalendarLocale = KalendarLocale.default(),
)
