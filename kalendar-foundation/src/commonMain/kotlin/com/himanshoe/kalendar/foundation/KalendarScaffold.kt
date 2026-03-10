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

package com.himanshoe.kalendar.foundation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.himanshoe.kalendar.foundation.component.config.KalendarDayLabelKonfig
import com.himanshoe.kalendar.foundation.locale.KalendarLocale
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

@Composable
fun KalendarScaffold(
    showDayLabel: Boolean,
    dayOfWeek: () -> List<DayOfWeek>,
    kalendarDayLabelKonfig: KalendarDayLabelKonfig,
    modifier: Modifier = Modifier,
    kalendarLocale: KalendarLocale = KalendarLocale.default(),
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
                items(displayDayOfWeek) { dayOfWeek ->
                    val localizedName = kalendarLocale.shortDayNames.getOrElse(dayOfWeek.ordinal) {
                        dayOfWeek.name.take(kalendarDayLabelKonfig.textCharCount)
                    }
                    Text(
                        text = localizedName.take(kalendarDayLabelKonfig.textCharCount),
                        modifier = Modifier.fillMaxWidth(),
                        style = kalendarDayLabelKonfig.textStyle
                    )
                }
            }
            items(items = displayDates) { date ->
                content(date)
            }
        }
    )
}
