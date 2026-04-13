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

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class KalendarSelectedDayRangeTest {

    @Test
    fun validRange_sameDay() {
        val date = LocalDate(2026, 6, 15)
        val range = KalendarSelectedDayRange(start = date, endInclusive = date)
        assertEquals(date, range.start)
        assertEquals(date, range.endInclusive)
    }

    @Test
    fun validRange_startBeforeEnd() {
        val start = LocalDate(2026, 6, 1)
        val end = LocalDate(2026, 6, 30)
        val range = KalendarSelectedDayRange(start = start, endInclusive = end)
        assertEquals(start, range.start)
        assertEquals(end, range.endInclusive)
    }

    @Test
    fun validRange_acrossMonths() {
        val start = LocalDate(2026, 6, 28)
        val end = LocalDate(2026, 7, 5)
        val range = KalendarSelectedDayRange(start = start, endInclusive = end)
        assertEquals(start, range.start)
        assertEquals(end, range.endInclusive)
    }

    @Test
    fun validRange_acrossYears() {
        val start = LocalDate(2026, 12, 30)
        val end = LocalDate(2026, 1, 3)
        val range = KalendarSelectedDayRange(start = start, endInclusive = end)
        assertEquals(start, range.start)
        assertEquals(end, range.endInclusive)
    }

    @Test
    fun inOperator_dateInRange() {
        val start = LocalDate(2026, 6, 1)
        val end = LocalDate(2026, 6, 30)
        val range = KalendarSelectedDayRange(start = start, endInclusive = end)
        assertTrue(LocalDate(2026, 6, 15) in range)
        assertTrue(start in range)
        assertTrue(end in range)
    }

    @Test
    fun invalidRange_endBeforeStart_throws() {
        val start = LocalDate(2026, 6, 15)
        val end = LocalDate(2026, 6, 10)
        assertFailsWith<IllegalArgumentException> {
            KalendarSelectedDayRange(start = start, endInclusive = end)
        }
    }

    @Test
    fun invalidRange_endInPreviousYear_throws() {
        val start = LocalDate(2026, 1, 1)
        val end = LocalDate(2024, 12, 31)
        assertFailsWith<IllegalArgumentException> {
            KalendarSelectedDayRange(start = start, endInclusive = end)
        }
    }
}
