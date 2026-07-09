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

package com.himanshoe.kalendar

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.himanshoe.kalendar.foundation.action.OnDaySelectionAction
import com.himanshoe.kalendar.foundation.component.config.KalendarConfig
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalTestApi::class)
class KalendarUiTest {

    private val today = LocalDate(2026, 7, 15)
    private val yesterday = LocalDate(2026, 7, 14)
    private val maxDate = LocalDate(2026, 7, 31)

    @Test
    fun oceanicDisabledPastDateNotClickable() = runComposeUiTest {
        var selected: LocalDate? = null
        setContent {
            Kalendar(
                type = KalendarType.Oceanic,
                selectedDate = today,
                onDaySelectionAction = OnDaySelectionAction.Single { date, _ ->
                    selected = date
                },
                config = KalendarConfig(
                    firstVisibleDate = today,
                    minDate = today,
                    maxDate = maxDate,
                    disabledDates = { it < today },
                ),
            )
        }

        onNodeWithTag("kalendar-day-$yesterday").assertIsDisplayed()
        onNodeWithTag("kalendar-day-$yesterday").assertIsNotEnabled()
        assertNull(selected)

        onNodeWithTag("kalendar-day-$today").assertIsEnabled()
        onNodeWithTag("kalendar-day-$today").performClick()
        assertEquals(today, selected)
    }

    @Test
    fun oceanicNextMonthArrowDisabledAtMaxDate() = runComposeUiTest {
        setContent {
            Kalendar(
                type = KalendarType.Oceanic,
                selectedDate = today,
                config = KalendarConfig(
                    firstVisibleDate = today,
                    minDate = LocalDate(2026, 1, 1),
                    maxDate = maxDate,
                    showArrows = true,
                ),
            )
        }

        onNodeWithContentDescription("Next month").assertIsNotEnabled()
        onNodeWithContentDescription("Previous month").assertIsEnabled()
    }

    @Test
    fun oceanicMondayFirstJanuaryGridIncludesPreviousYearPad() = runComposeUiTest {
        val jan = LocalDate(2026, 1, 15)
        setContent {
            Kalendar(
                type = KalendarType.Oceanic,
                selectedDate = jan,
                config = KalendarConfig(
                    firstVisibleDate = jan,
                    startDayOfWeek = kotlinx.datetime.DayOfWeek.MONDAY,
                    showArrows = true,
                ),
            )
        }

        onNodeWithTag("kalendar-day-2025-12-29").assertIsDisplayed()
        onNodeWithTag("kalendar-day-2026-01-01").assertIsDisplayed()
        onNodeWithText("january '26", ignoreCase = true).assertIsDisplayed()
    }
}
