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

package com.himanshoe.kalendar.foundation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.himanshoe.kalendar.foundation.color.KalendarColor

/**
 * A small circular dot indicator rendered below a day number to represent an event.
 *
 * Multiple indicators can appear side by side for days with several events.
 * The opacity varies by [index] to visually distinguish stacked indicators.
 *
 * @param index The zero-based index of this indicator among siblings.
 * @param size The base size of the parent day cell, used to derive the indicator size.
 * @param color The default indicator colour from configuration.
 * @param modifier Modifier applied to the indicator dot.
 * @param eventColor An optional per-event colour that overrides [color].
 */
@Composable
fun KalendarIndicator(
    index: Int,
    size: Dp,
    color: KalendarColor,
    modifier: Modifier = Modifier,
    eventColor: KalendarColor? = null,
) {
    val effectiveColor = eventColor ?: color
    val brush = Brush.linearGradient(effectiveColor.value.map { it.copy(alpha = (index + 1) * 0.3f) })
    Box(
        modifier = modifier
            .padding(horizontal = 1.dp)
            .clip(shape = CircleShape)
            .background(brush = brush)
            .size(size = size.div(12))
    )
}
