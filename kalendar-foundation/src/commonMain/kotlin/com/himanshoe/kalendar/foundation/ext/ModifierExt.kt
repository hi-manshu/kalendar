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
 * Forces the layout to be circular by setting both dimensions to the maximum of width and height,
 * then centring the child within the resulting square.
 */
fun Modifier.circleLayout() =
    layout { measurable, constraints ->
        // Measure the composable
        val placeable = measurable.measure(constraints)

        // Get the current dimensions
        val height = placeable.height
        val width = placeable.width

        // Calculate the new diameter to make the layout circular
        val diameter = maxOf(height, width)

        // Assign the new dimensions and center the composable
        layout(diameter, diameter) {
            placeable.placeRelative(
                (diameter - width) / 2,
                (diameter - height) / 2
            )
        }
    }

/**
 * Applies the appropriate background colour to a day cell based on its selection state.
 *
 * The background is determined by whether the date is selected, part of a selected range,
 * or in a multi-selection list. Unselected dates are transparent.
 *
 * @param date The date this modifier is applied to.
 * @param selected Whether this date is the primary single-selected date.
 * @param colors The gradient colours for the selected background.
 * @param selectedRange The currently selected date range, if any.
 * @param selectedDates The list of dates selected in multi-select mode.
 */
fun Modifier.dayBackgroundColor(
    date: LocalDate,
    selected: Boolean,
    colors: List<Color>,
    selectedRange: KalendarSelectedDayRange?,
    selectedDates: List<LocalDate>,
): Modifier {
    val inRange = selectedRange?.let { date == it.start || date == it.end } ?: false
    val isSelectedDate = selectedDates.contains(date)

    val backgroundBrush = when {
        selected -> Brush.linearGradient(colors)
        isSelectedDate -> Brush.linearGradient(colors)
        selectedRange != null && date in selectedRange.start..selectedRange.end -> {
            val alpha = if (inRange) FULL_ALPHA else TOWNED_DOWN_ALPHA
            Brush.linearGradient(colors.map { it.copy(alpha = alpha) })
        }

        else -> Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color.Transparent)
        )
    }

    return this.then(
        background(brush = backgroundBrush)
    )
}
