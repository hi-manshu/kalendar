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

package com.himanshoe.kalendar.foundation.ext

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import com.himanshoe.kalendar.foundation.action.KalendarSelectedDayRange
import kotlinx.datetime.LocalDate

private const val FULL_ALPHA = 1f
private const val TOWNED_DOWN_ALPHA = 0.5F

/**
 * Forces the composable into a square bounding box whose side equals `max(width, height)`,
 * then centres the original content inside it. Used by day cells to ensure they remain
 * perfectly circular regardless of the text content size.
 */
fun Modifier.circleLayout() =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val height = placeable.height
        val width = placeable.width
        val diameter = maxOf(height, width)
        layout(diameter, diameter) {
            placeable.placeRelative(
                (diameter - width) / 2,
                (diameter - height) / 2
            )
        }
    }

/**
 * Applies a background [Brush] to a day cell based on its selection state.
 *
 * - **Selected** (`selected == true` or `date` is in `selectedDates`): full-opacity gradient.
 * - **In range** (date falls inside `selectedRange` but is not a boundary): 50 % opacity gradient.
 * - **Range boundary** (`date == selectedRange.start` or `date == selectedRange.endInclusive`):
 *   full-opacity gradient.
 * - **Otherwise**: fully transparent background.
 *
 * @param date The [LocalDate] represented by this cell.
 * @param selected Whether this date is the primary selected date.
 * @param colors The gradient stop colours drawn by [Brush.linearGradient].
 * @param selectedRange The currently active [KalendarSelectedDayRange], or `null`.
 * @param selectedDates Additional individually selected dates (multi-select mode).
 */
fun Modifier.dayBackgroundColor(
    date: LocalDate,
    selected: Boolean,
    colors: List<Color>,
    selectedRange: KalendarSelectedDayRange?,
    selectedDates: List<LocalDate>,
): Modifier {
    val inRange = selectedRange?.let { date == it.start || date == it.endInclusive } ?: false
    val isSelectedDate = selectedDates.contains(date)

    val backgroundBrush = when {
        selected -> Brush.linearGradient(colors)
        isSelectedDate -> Brush.linearGradient(colors)
        selectedRange != null && date in selectedRange -> {
            val alpha = if (inRange) FULL_ALPHA else TOWNED_DOWN_ALPHA
            Brush.linearGradient(colors.map { it.copy(alpha = alpha) })
        }
        else -> Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Transparent))
    }

    return this.then(background(brush = backgroundBrush))
}
