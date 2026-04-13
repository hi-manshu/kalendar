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

package com.himanshoe.kalendar.foundation.color

import androidx.compose.ui.graphics.Color

/**
 * A sealed class representing different types of colors.
 *
 * @property value A list of colors used for the color representation.
 */
sealed class KalendarColor(open val value: List<Color> = emptyList()) {
    /**
     * A data class representing a solid color.
     *
     * @property color The solid color.
     */
    data class Solid(val color: Color) : KalendarColor(listOf(color, color))

    /**
     * A data class representing a gradient color.
     *
     * @property value A list of colors used for the gradient.
     */
    data class Gradient(override val value: List<Color>) : KalendarColor(value)
}

/**
 * Extension function to convert a Color to a Solid KalendarColor.
 *
 * @return A Solid KalendarColor with the given color.
 */
fun Color.asSolidColor() = KalendarColor.Solid(this)

/**
 * Extension function to convert a list of Colors to a Gradient KalendarColor.
 *
 * @return A Gradient KalendarColor with the given list of colors.
 */
fun List<Color>.asGradientColor() = KalendarColor.Gradient(this)
