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
 * Visual configuration for the calendar header row.
 *
 * @property textStyle Text style applied to the header title (month/year or custom text).
 * @property centerAligned When true, the title is centred between the navigation arrows.
 *   When false, the title is left-aligned and arrows are grouped on the right.
 */
data class KalendarHeaderKonfig(
    val textStyle: TextStyle,
    val centerAligned: Boolean
) {
    companion object {

        fun default() = KalendarHeaderKonfig(
            centerAligned = true,
            textStyle = TextStyle(
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Start,
                fontSize = 20.sp,
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFF413D4B),
                        Color(0xFFD8A29E)
                    )
                ),
            ),
        )
    }
}
