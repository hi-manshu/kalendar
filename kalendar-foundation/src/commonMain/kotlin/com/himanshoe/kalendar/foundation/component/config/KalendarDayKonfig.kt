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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.himanshoe.kalendar.foundation.color.KalendarColor
import com.himanshoe.kalendar.foundation.color.asSolidChartColor

/**
 * Visual configuration for individual day cells in the calendar grid.
 *
 * @property size The minimum size (width and height) of each day cell.
 * @property selectedTextColor Text colour applied to the selected day's number.
 * @property borderColor Border colour for the "today" indicator ring.
 * @property indicatorColor Default colour for event indicator dots.
 * @property textStyle Base text style for day numbers.
 * @property selectedBackgroundColor Background colour applied to selected day cells.
 * @property disabledTextColor Text colour for days outside the enabled date range.
 */
data class KalendarDayKonfig(
    val size: Dp,
    val selectedTextColor: KalendarColor,
    val borderColor: KalendarColor,
    val indicatorColor: KalendarColor,
    val textStyle: TextStyle,
    val selectedBackgroundColor: KalendarColor,
    val disabledTextColor: KalendarColor = Color(0xFFBDBDBD).asSolidChartColor(),
) {
    companion object {

        fun default() = KalendarDayKonfig(
            size = 56.dp,
            textStyle = TextStyle(
                fontSize = 16.sp,
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFF413D4B),
                        Color(0xFFD8A29E)
                    )
                ),
            ),
            indicatorColor = Color(0xFFD8A29E).asSolidChartColor(),
            selectedTextColor = Color(0xFF413D4B).asSolidChartColor(),
            borderColor = Color(0xFFC39EA1).asSolidChartColor(),
            selectedBackgroundColor = Color(0xFFF7CFD3).asSolidChartColor(),
            disabledTextColor = Color(0xFFBDBDBD).asSolidChartColor(),
        )
    }
}
