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
import kotlin.test.assertTrue

class KalendarUtilsTest {

    @Test
    fun getMonthDates_june2026_sundayStart_correctCount() {
        val june = LocalDate(2026, 6, 1)
        val dates = getMonthDates(june, DayOfWeek.MONDAY)
        assertEquals(30, dates.size)
        assertEquals(LocalDate(2026, 6, 1), dates.first())
        assertEquals(LocalDate(2026, 6, 30), dates.last())
    }

    @Test
    fun getMonthDates_may2026_sundayStart_hasPaddingForWednesdayStart() {
        val may = LocalDate(2026, 5, 1)
        val dates = getMonthDates(may, DayOfWeek.MONDAY)
        assertTrue(dates.first() < may)
        val mayDates = dates.filter { it.month == Month.MAY }
        assertEquals(31, mayDates.size)
    }

    @Test
    fun getMonthDates_allDatesInCorrectMonth_orPaddingFromPreviousMonth() {
        val july = LocalDate(2026, 7, 1)
        val dates = getMonthDates(july, DayOfWeek.MONDAY)
        val julyDates = dates.filter { it.month == Month.JULY }
        assertEquals(31, julyDates.size)
    }

    @Test
    fun getMonthDates_datesAreConsecutive() {
        val march = LocalDate(2026, 3, 1)
        val dates = getMonthDates(march, DayOfWeek.MONDAY)
        for (i in 1 until dates.size) {
            val prev = dates[i - 1]
            val curr = dates[i]
            assertEquals(prev.toEpochDays() + 1, curr.toEpochDays())
        }
    }

    @Test
    fun getWeekDates_alwaysReturnsSeven() {
        val date = LocalDate(2026, 6, 15)
        val week = getWeekDates(date, DayOfWeek.MONDAY)
        assertEquals(7, week.size)
    }

    @Test
    fun getWeekDates_sundayStart_firstDayIsSunday() {
        val wednesday = LocalDate(2026, 6, 11)
        val week = getWeekDates(wednesday, DayOfWeek.MONDAY)
        assertEquals(DayOfWeek.MONDAY, week.first().dayOfWeek)
        assertEquals(DayOfWeek.SATURDAY, week.last().dayOfWeek)
    }

    @Test
    fun getWeekDates_mondayStart_firstDayIsMonday() {
        val wednesday = LocalDate(2026, 6, 11)
        val week = getWeekDates(wednesday, DayOfWeek.MONDAY)
        assertEquals(DayOfWeek.MONDAY, week.first().dayOfWeek)
        assertEquals(DayOfWeek.MONDAY, week.last().dayOfWeek)
    }

    @Test
    fun getWeekDates_containsTheInputDate() {
        val date = LocalDate(2026, 6, 15)
        val week = getWeekDates(date, DayOfWeek.MONDAY)
        assertTrue(date in week)
    }

    @Test
    fun getWeekDates_datesAreConsecutive() {
        val date = LocalDate(2026, 6, 15)
        val week = getWeekDates(date, DayOfWeek.MONDAY)
        for (i in 1 until week.size) {
            assertEquals(week[i - 1].toEpochDays() + 1, week[i].toEpochDays())
        }
    }

    @Test
    fun rotate_byZero_returnsSameOrder() {
        val original = DayOfWeek.entries.toList()
        val rotated = original.rotate(0)
        assertEquals(original, rotated)
    }

    @Test
    fun rotate_bySeven_returnsSameOrder() {
        val original = DayOfWeek.entries.toList()
        assertEquals(original, original.rotate(7))
    }

    @Test
    fun rotate_sundayStart_mondayIsFirst() {
        val rotated = DayOfWeek.entries.rotate(DayOfWeek.MONDAY.ordinal)
        assertEquals(DayOfWeek.MONDAY, rotated.first())
    }

    @Test
    fun rotate_mondayStart_mondayIsFirst() {
        val rotated = DayOfWeek.entries.rotate(DayOfWeek.MONDAY.ordinal)
        assertEquals(DayOfWeek.MONDAY, rotated.first())
    }

    @Test
    fun rotate_preservesAllElements() {
        val original = DayOfWeek.entries.toList()
        val rotated = original.rotate(3)
        assertEquals(original.toSet(), rotated.toSet())
        assertEquals(7, rotated.size)
    }
}
