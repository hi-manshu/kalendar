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

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KalendarSeasonTest {

    @Test
    fun seasonPeriodOf_springMonths() {
        assertEquals(
            KalendarSeasonPeriod(2026, KalendarSeason.Spring),
            seasonPeriodOf(LocalDate(2026, 3, 1)),
        )
        assertEquals(
            KalendarSeasonPeriod(2026, KalendarSeason.Spring),
            seasonPeriodOf(LocalDate(2026, 4, 15)),
        )
        assertEquals(
            KalendarSeasonPeriod(2026, KalendarSeason.Spring),
            seasonPeriodOf(LocalDate(2026, 5, 31)),
        )
    }

    @Test
    fun seasonPeriodOf_summerMonths() {
        assertEquals(
            KalendarSeasonPeriod(2026, KalendarSeason.Summer),
            seasonPeriodOf(LocalDate(2026, 6, 1)),
        )
        assertEquals(
            KalendarSeasonPeriod(2026, KalendarSeason.Summer),
            seasonPeriodOf(LocalDate(2026, 8, 31)),
        )
    }

    @Test
    fun seasonPeriodOf_autumnMonths() {
        assertEquals(
            KalendarSeasonPeriod(2026, KalendarSeason.Autumn),
            seasonPeriodOf(LocalDate(2026, 9, 1)),
        )
        assertEquals(
            KalendarSeasonPeriod(2026, KalendarSeason.Autumn),
            seasonPeriodOf(LocalDate(2026, 11, 30)),
        )
    }

    @Test
    fun seasonPeriodOf_winterCrossesYear() {
        assertEquals(
            KalendarSeasonPeriod(2026, KalendarSeason.Winter),
            seasonPeriodOf(LocalDate(2026, 12, 1)),
        )
        assertEquals(
            KalendarSeasonPeriod(2025, KalendarSeason.Winter),
            seasonPeriodOf(LocalDate(2026, 1, 15)),
        )
        assertEquals(
            KalendarSeasonPeriod(2025, KalendarSeason.Winter),
            seasonPeriodOf(LocalDate(2026, 2, 28)),
        )
    }

    @Test
    fun period_monthsAndYears_winterSpan() {
        val winter = KalendarSeasonPeriod(2026, KalendarSeason.Winter)
        assertEquals(
            listOf(Month.DECEMBER, Month.JANUARY, Month.FEBRUARY),
            winter.months,
        )
        assertEquals(listOf(2026, 2027, 2027), winter.years)
        assertEquals(LocalDate(2026, 12, 1), winter.startDate)
        assertEquals(LocalDate(2027, 2, 28), winter.endDate)
    }

    @Test
    fun period_monthsAndYears_spring() {
        val spring = KalendarSeasonPeriod(2026, KalendarSeason.Spring)
        assertEquals(
            listOf(Month.MARCH, Month.APRIL, Month.MAY),
            spring.months,
        )
        assertEquals(listOf(2026, 2026, 2026), spring.years)
        assertEquals(LocalDate(2026, 3, 1), spring.startDate)
        assertEquals(LocalDate(2026, 5, 31), spring.endDate)
    }

    @Test
    fun period_title_formatsWinterRange() {
        assertEquals("Winter 2026–2027", KalendarSeasonPeriod(2026, KalendarSeason.Winter).title())
        assertEquals("Spring 2026", KalendarSeasonPeriod(2026, KalendarSeason.Spring).title())
        assertEquals("Summer 2026", KalendarSeasonPeriod(2026, KalendarSeason.Summer).title())
        assertEquals("Autumn 2026", KalendarSeasonPeriod(2026, KalendarSeason.Autumn).title())
    }

    @Test
    fun period_previousNext_cycleAroundYear() {
        val spring = KalendarSeasonPeriod(2026, KalendarSeason.Spring)
        assertEquals(KalendarSeasonPeriod(2025, KalendarSeason.Winter), spring.previous())
        assertEquals(KalendarSeasonPeriod(2026, KalendarSeason.Summer), spring.next())

        val winter = KalendarSeasonPeriod(2026, KalendarSeason.Winter)
        assertEquals(KalendarSeasonPeriod(2026, KalendarSeason.Autumn), winter.previous())
        assertEquals(KalendarSeasonPeriod(2027, KalendarSeason.Spring), winter.next())
    }

    @Test
    fun seasonsBetween_adjacentAndYearSpan() {
        val spring = KalendarSeasonPeriod(2026, KalendarSeason.Spring)
        val summer = KalendarSeasonPeriod(2026, KalendarSeason.Summer)
        val nextSpring = KalendarSeasonPeriod(2027, KalendarSeason.Spring)
        assertEquals(0, seasonsBetween(spring, spring))
        assertEquals(1, seasonsBetween(spring, summer))
        assertEquals(-1, seasonsBetween(summer, spring))
        assertEquals(4, seasonsBetween(spring, nextSpring))
    }

    @Test
    fun daysInMonth_leapAndNonLeap() {
        assertEquals(29, daysInMonth(2024, Month.FEBRUARY))
        assertEquals(28, daysInMonth(2026, Month.FEBRUARY))
        assertEquals(31, daysInMonth(2026, Month.JANUARY))
        assertEquals(30, daysInMonth(2026, Month.APRIL))
    }

    @Test
    fun isLeapYear_rules() {
        assertTrue(isLeapYear(2024))
        assertFalse(isLeapYear(2026))
        assertFalse(isLeapYear(1900))
        assertTrue(isLeapYear(2000))
    }

    @Test
    fun winterEndDate_leapYearFebruary() {
        val winter = KalendarSeasonPeriod(2023, KalendarSeason.Winter)
        assertEquals(LocalDate(2024, 2, 29), winter.endDate)
    }

    @Test
    fun defaultPalette_distinctPerSeason() {
        val spring = KalendarSeason.Spring.defaultPalette()
        val winter = KalendarSeason.Winter.defaultPalette()
        assertTrue(spring.primary != winter.primary)
        assertTrue(spring.selectedBackground != winter.selectedBackground)
    }
}
