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

package com.himanshoe.kalendar.foundation.action

import com.himanshoe.kalendar.foundation.component.config.KalendarConfig
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KalendarDateInteractableTest {

    private val today = LocalDate(2026, 7, 9)

    @Test
    fun disabledDates_predicate_blocks() {
        val config = KalendarConfig(disabledDates = { it < today })
        assertFalse(LocalDate(2026, 7, 8).isDateInteractable(config))
        assertTrue(today.isDateInteractable(config))
        assertTrue(LocalDate(2026, 7, 10).isDateInteractable(config))
    }

    @Test
    fun minMax_bounds_block() {
        val config = KalendarConfig(
            minDate = LocalDate(2026, 7, 1),
            maxDate = LocalDate(2026, 7, 31),
        )
        assertFalse(LocalDate(2026, 6, 30).isDateInteractable(config))
        assertTrue(LocalDate(2026, 7, 15).isDateInteractable(config))
        assertFalse(LocalDate(2026, 8, 1).isDateInteractable(config))
    }

    @Test
    fun nonPrimaryPeriod_alwaysBlocked() {
        val config = KalendarConfig()
        assertFalse(today.isDateInteractable(config, isPrimaryPeriod = false))
        assertTrue(today.isDateInteractable(config, isPrimaryPeriod = true))
    }

    @Test
    fun rangeFullyAllowed_detectsHole() {
        val allowed = setOf(
            LocalDate(2026, 7, 10),
            LocalDate(2026, 7, 11),
            LocalDate(2026, 7, 13),
        )
        val isAllowed: (LocalDate) -> Boolean = { it in allowed }
        assertFalse(
            isRangeFullyAllowed(
                LocalDate(2026, 7, 10),
                LocalDate(2026, 7, 13),
                isAllowed,
            )
        )
        assertTrue(
            isRangeFullyAllowed(
                LocalDate(2026, 7, 10),
                LocalDate(2026, 7, 11),
                isAllowed,
            )
        )
    }
}
