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

package com.himanshoe.kalendar.foundation.component.config

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import kotlinx.datetime.DayOfWeek

/**
 * Visual and locale configuration for the row of day-of-week column labels shown above the
 * calendar grid (e.g. "Mo", "Tu", "We", …).
 *
 * @param textStyle The [TextStyle] applied to each label. The default uses a gradient brush.
 * @param textCharCount The maximum number of characters taken from the day name when
 *   [dayNameFormatter] is `null`. For example `2` produces "MO", "TU" etc. Defaults to `2`.
 * @param centerAligned When `true` the label text is centred within its cell. Defaults to `true`.
 * @param dayNameFormatter An optional lambda that converts a [DayOfWeek] to its display string.
 *   When non-null this fully replaces the default `name.take(textCharCount)` behaviour,
 *   allowing locale-aware formatting:
 *   ```kotlin
 *   dayNameFormatter = { dayOfWeek ->
 *       dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
 *   }
 *   ```
 *   Defaults to `null` (falls back to `textCharCount`).
 */
@Immutable
data class KalendarDayLabelConfig(
    val textStyle: TextStyle,
    val textCharCount: Int,
    val centerAligned: Boolean,
    val dayNameFormatter: ((DayOfWeek) -> String)? = null,
) {
    companion object {
        /**
         * Returns a [KalendarDayLabelConfig] populated with the default visual values used by the
         * built-in calendar themes.
         */
        fun default() = KalendarDayLabelConfig(
            centerAligned = true,
            textCharCount = 2,
            textStyle = TextStyle(
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFF613D4B),
                        Color(0xFFD8A26E)
                    )
                ),
            ),
        )
    }
}
