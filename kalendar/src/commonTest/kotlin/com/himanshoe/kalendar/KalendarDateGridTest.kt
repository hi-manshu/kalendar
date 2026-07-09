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

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KalendarDateGridTest {

    @Test
    fun getMonthDates_februaryLeapYear_has29Days() {
        val dates = getMonthDates(LocalDate(2024, 2, 1), DayOfWeek.MONDAY)
        val feb = dates.filter { it.month == Month.FEBRUARY && it.year == 2024 }
        assertEquals(29, feb.size)
        assertEquals(0, dates.size % 7)
    }

    @Test
    fun getMonthDates_februaryNonLeap_has28Days() {
        val dates = getMonthDates(LocalDate(2026, 2, 1), DayOfWeek.MONDAY)
        val feb = dates.filter { it.month == Month.FEBRUARY && it.year == 2026 }
        assertEquals(28, feb.size)
    }

    @Test
    fun getMonthDates_trailingPadding_fromNextMonth() {
        // June 2026 starts on Monday → no leading pad; 30 days → 5 trailing (to 35)
        val dates = getMonthDates(LocalDate(2026, 6, 1), DayOfWeek.MONDAY)
        assertEquals(35, dates.size)
        val trailing = dates.filter { it.month == Month.JULY }
        assertEquals(5, trailing.size)
        assertEquals(LocalDate(2026, 7, 1), trailing.first())
    }

    @Test
    fun getMonthDates_leadingPadding_fromPreviousMonth() {
        val dates = getMonthDates(LocalDate(2026, 5, 1), DayOfWeek.MONDAY)
        val leading = dates.takeWhile { it.month != Month.MAY }
        assertTrue(leading.isNotEmpty())
        assertTrue(leading.all { it.month == Month.APRIL })
        assertEquals(DayOfWeek.MONDAY, dates.first().dayOfWeek)
    }

    @Test
    fun startOfWeekContaining_januaryFirstThursday_returnsPreviousYearMonday() {
        val jan1 = LocalDate(2026, 1, 1)
        assertEquals(DayOfWeek.THURSDAY, jan1.dayOfWeek)
        val weekStart = startOfWeekContaining(jan1, DayOfWeek.MONDAY)
        assertEquals(LocalDate(2025, 12, 29), weekStart)
        assertEquals(DayOfWeek.MONDAY, weekStart.dayOfWeek)
        assertEquals(2025, weekStart.year)
    }

    @Test
    fun getMonthDates_january2026_firstGridMonday_isInPreviousCalendarMonth() {
        val dates = getMonthDates(LocalDate(2026, 1, 1), DayOfWeek.MONDAY)
        assertEquals(LocalDate(2025, 12, 29), dates.first())
        assertEquals(DayOfWeek.MONDAY, dates.first().dayOfWeek)
        assertEquals(Month.DECEMBER, dates.first().month)
        assertEquals(2025, dates.first().year)
        assertTrue(LocalDate(2026, 1, 1) in dates)
        assertEquals(0, dates.size % 7)
    }

    @Test
    fun getMonthDates_january2026_firstWednesdayOnGrid_isPreviousYearPad() {
        val dates = getMonthDates(LocalDate(2026, 1, 1), DayOfWeek.MONDAY)
        val firstWednesday = dates.first { it.dayOfWeek == DayOfWeek.WEDNESDAY }
        assertEquals(LocalDate(2025, 12, 31), firstWednesday)
    }

    @Test
    fun getMonthDates_acceptsMidMonthAnchor() {
        // Should still build calendar for that month
        val dates = getMonthDates(LocalDate(2026, 6, 15), DayOfWeek.SUNDAY)
        val june = dates.filter { it.month == Month.JUNE }
        assertEquals(30, june.size)
        assertEquals(DayOfWeek.SUNDAY, dates.first().dayOfWeek)
    }

    @Test
    fun getWeekDates_allStartDays_firstMatchesConfig() {
        val mid = LocalDate(2026, 6, 15)
        for (start in DayOfWeek.entries) {
            val week = getWeekDates(mid, start)
            assertEquals(7, week.size)
            assertEquals(start, week.first().dayOfWeek)
            assertTrue(mid in week)
        }
    }

    @Test
    fun weeksBetweenAligned_previousWeek_negative() {
        val mon = LocalDate(2026, 6, 15)
        val prev = LocalDate(2026, 6, 8)
        assertEquals(-1, weeksBetweenAligned(mon, prev, DayOfWeek.MONDAY))
    }

    @Test
    fun weeksBetweenAligned_multipleWeeks() {
        val a = LocalDate(2026, 1, 5)
        val b = LocalDate(2026, 2, 2)
        assertEquals(4, weeksBetweenAligned(a, b, DayOfWeek.MONDAY))
    }

    @Test
    fun monthsBetween_sameMonth_zero() {
        assertEquals(0, monthsBetween(LocalDate(2026, 6, 1), LocalDate(2026, 6, 1)))
    }

    @Test
    fun monthsBetween_crossYear() {
        assertEquals(2, monthsBetween(LocalDate(2025, 11, 1), LocalDate(2026, 1, 1)))
        assertEquals(-2, monthsBetween(LocalDate(2026, 1, 1), LocalDate(2025, 11, 1)))
    }

    @Test
    fun isDateOutOfBounds_nullBounds_neverOut() {
        assertFalse(isDateOutOfBounds(LocalDate(1900, 1, 1), null, null))
        assertFalse(isDateOutOfBounds(LocalDate(2100, 12, 31), null, null))
    }

    @Test
    fun isDateOutOfBounds_onlyMin() {
        val min = LocalDate(2026, 6, 10)
        assertTrue(isDateOutOfBounds(LocalDate(2026, 6, 9), min, null))
        assertFalse(isDateOutOfBounds(min, min, null))
        assertFalse(isDateOutOfBounds(LocalDate(2026, 6, 11), min, null))
    }

    @Test
    fun isDateOutOfBounds_onlyMax() {
        val max = LocalDate(2026, 6, 10)
        assertFalse(isDateOutOfBounds(LocalDate(2026, 6, 9), null, max))
        assertFalse(isDateOutOfBounds(max, null, max))
        assertTrue(isDateOutOfBounds(LocalDate(2026, 6, 11), null, max))
    }

    @Test
    fun rotate_sundayOrdinal_startsSunday() {
        val rotated = DayOfWeek.entries.rotate(DayOfWeek.SUNDAY.ordinal)
        assertEquals(DayOfWeek.SUNDAY, rotated.first())
        assertEquals(DayOfWeek.SATURDAY, rotated.last())
    }
}
