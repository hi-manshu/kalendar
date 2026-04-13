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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.himanshoe.kalendar.foundation.color.KalendarColor

/**
 * A small circular dot rendered below a day number to indicate that one or more events
 * fall on that day.
 *
 * Up to three dots are shown per cell; when an event supplies an
 * [com.himanshoe.kalendar.foundation.event.KalendarEvent.eventColor] that colour is used
 * instead of the default [color].
 *
 * @param index The zero-based position of this dot among sibling indicators for the same day.
 *   Used to vary the alpha so that successive dots fade slightly.
 * @param size The size of the parent day cell ([com.himanshoe.kalendar.foundation.component.config.KalendarDayConfig.size]).
 *   The dot diameter is derived from this value (`size / 12`).
 * @param color The fallback [KalendarColor] to use when [overrideColor] is `null`.
 * @param modifier [Modifier] applied to the dot container.
 * @param overrideColor When non-null this solid [Color] is used in place of [color], allowing
 *   per-event colour customisation via [com.himanshoe.kalendar.foundation.event.KalendarEvent.eventColor].
 *   Defaults to `null`.
 */
@Composable
fun KalendarIndicator(
    index: Int,
    size: Dp,
    color: KalendarColor,
    modifier: Modifier = Modifier,
    overrideColor: Color? = null,
) {
    val brush = if (overrideColor != null) {
        Brush.linearGradient(listOf(overrideColor, overrideColor))
    } else {
        Brush.linearGradient(color.value.map { it.copy(alpha = (index + 1) * 0.3f) })
    }
    Box(
        modifier = modifier
            .padding(horizontal = 1.dp)
            .clip(shape = CircleShape)
            .background(brush = brush)
            .size(size = size.div(12))
    )
}
