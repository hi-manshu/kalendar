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

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

/**
 * Visual configuration for the day-of-week label row displayed above the calendar grid.
 *
 * @property textStyle Text style for the day labels (e.g., "Mo", "Tu").
 * @property textCharCount Maximum number of characters to display for each day name.
 * @property centerAligned Whether to centre-align the labels within their columns.
 */
data class KalendarDayLabelKonfig(
    val textStyle: TextStyle,
    val textCharCount: Int,
    val centerAligned: Boolean
) {
    companion object {
        fun default() = KalendarDayLabelKonfig(
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
