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

package com.himanshoe.kalendar.foundation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.himanshoe.kalendar.foundation.component.config.KalendarDayLabelConfig
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

/**
 * The structural backbone of every Kalendar layout variant.
 *
 * Renders an optional row of day-of-week column headers followed by the date cells provided
 * by [content]. The grid is always 7 columns wide.
 *
 * @param showDayLabel When `true` a row of abbreviated day-of-week labels is rendered above
 *   the date grid (e.g. "Mo", "Tu", "We", …). Defaults to `true`.
 * @param dayOfWeek A lambda that returns the ordered list of [DayOfWeek] values to use as
 *   column headers. The order should match the [com.himanshoe.kalendar.foundation.component.config.KalendarConfig.startDayOfWeek]
 *   setting of the parent calendar.
 * @param dayLabelConfig Visual and locale configuration for the day-of-week label row.
 *   Supply a [com.himanshoe.kalendar.foundation.component.config.KalendarDayLabelConfig.dayNameFormatter]
 *   to enable locale-aware label text.
 * @param modifier [Modifier] applied to the [LazyVerticalGrid] container.
 * @param dates A lambda that returns the list of [LocalDate] values to render. The list
 *   typically includes padding dates from the previous or next month to fill the first and
 *   last rows of the grid.
 * @param content The composable slot called once per date. Implementations are responsible
 *   for handling padding dates (dates outside the current month) appropriately.
 */
@Composable
fun KalendarScaffold(
    showDayLabel: Boolean,
    dayOfWeek: () -> List<DayOfWeek>,
    dayLabelConfig: KalendarDayLabelConfig,
    modifier: Modifier = Modifier,
    dates: () -> List<LocalDate>,
    content: @Composable (LocalDate) -> Unit,
) {
    val displayDates = dates()
    val displayDayOfWeek = dayOfWeek()

    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(7),
        horizontalArrangement = Arrangement.Center,
        content = {
            if (showDayLabel) {
                items(displayDayOfWeek) { day ->
                    val label = dayLabelConfig.dayNameFormatter?.invoke(day)
                        ?: day.name.take(dayLabelConfig.textCharCount)
                    Text(
                        text = label,
                        modifier = Modifier.fillMaxWidth(),
                        style = dayLabelConfig.textStyle
                    )
                }
            }
            items(items = displayDates, key = { it.toEpochDays() }) { date ->
                content(date)
            }
        }
    )
}
