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

package com.himanshoe.kalendar.sync

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class KalendarRecurrenceExpanderTest {

    @Test
    fun noRule_returnsSelfWhenInRange() {
        val event = BasicKalendarSyncEvent(
            date = LocalDate(2026, 6, 15),
            eventName = "Once",
        )
        val inRange = event.expandOccurrences(LocalDate(2026, 6, 1), LocalDate(2026, 6, 30))
        assertEquals(1, inRange.size)
        assertEquals(event, inRange.first())

        val outOfRange = event.expandOccurrences(LocalDate(2026, 7, 1), LocalDate(2026, 7, 31))
        assertTrue(outOfRange.isEmpty())
    }

    @Test
    fun daily_intervalTwo() {
        val event = BasicKalendarSyncEvent(
            date = LocalDate(2026, 1, 1),
            eventName = "Every other day",
            recurrenceRule = KalendarRule(
                frequency = KalendarRecurrenceFrequency.DAILY,
                interval = 2,
                count = 4,
            ),
        )
        val dates = event.expandOccurrences(LocalDate(2026, 1, 1), LocalDate(2026, 1, 31))
            .map { it.date }
        assertEquals(
            listOf(
                LocalDate(2026, 1, 1),
                LocalDate(2026, 1, 3),
                LocalDate(2026, 1, 5),
                LocalDate(2026, 1, 7),
            ),
            dates,
        )
    }

    @Test
    fun weeklyWithIntervalAndByDay_skipsAlternateWeeks() {
        val template = BasicKalendarSyncEvent(
            date = LocalDate(2026, 1, 5), // Monday
            eventName = "Biweekly",
            recurrenceRule = KalendarRule(
                frequency = KalendarRecurrenceFrequency.WEEKLY,
                interval = 2,
                byDay = listOf(KalendarWeekDay.MONDAY),
            ),
        )
        val dates = template.expandOccurrences(
            rangeStart = LocalDate(2026, 1, 1),
            rangeEnd = LocalDate(2026, 2, 28),
        ).map { it.date }

        assertTrue(LocalDate(2026, 1, 5) in dates)
        assertTrue(LocalDate(2026, 1, 12) !in dates)
        assertTrue(LocalDate(2026, 1, 19) in dates)
        assertTrue(LocalDate(2026, 1, 26) !in dates)
    }

    @Test
    fun weeklyWithMultipleByDays_sameWeek() {
        val event = BasicKalendarSyncEvent(
            date = LocalDate(2026, 1, 5), // Monday
            eventName = "Mon Wed",
            recurrenceRule = KalendarRule(
                frequency = KalendarRecurrenceFrequency.WEEKLY,
                byDay = listOf(KalendarWeekDay.MONDAY, KalendarWeekDay.WEDNESDAY),
                count = 4,
            ),
        )
        val dates = event.expandOccurrences(LocalDate(2026, 1, 1), LocalDate(2026, 1, 31))
            .map { it.date }
        assertEquals(
            listOf(
                LocalDate(2026, 1, 5),
                LocalDate(2026, 1, 7),
                LocalDate(2026, 1, 12),
                LocalDate(2026, 1, 14),
            ),
            dates,
        )
    }

    @Test
    fun countIncludesOccurrencesBeforeWindow() {
        val template = BasicKalendarSyncEvent(
            date = LocalDate(2026, 1, 1),
            eventName = "Daily",
            recurrenceRule = KalendarRule(
                frequency = KalendarRecurrenceFrequency.DAILY,
                count = 3,
            ),
        )
        val occurrences = template.expandOccurrences(
            rangeStart = LocalDate(2026, 1, 3),
            rangeEnd = LocalDate(2026, 1, 31),
        )
        assertEquals(listOf(LocalDate(2026, 1, 3)), occurrences.map { it.date })
    }

    @Test
    fun untilStopsExpansion() {
        val event = BasicKalendarSyncEvent(
            date = LocalDate(2026, 1, 1),
            eventName = "Daily until",
            recurrenceRule = KalendarRule(
                frequency = KalendarRecurrenceFrequency.DAILY,
                until = LocalDate(2026, 1, 3),
            ),
        )
        val dates = event.expandOccurrences(LocalDate(2026, 1, 1), LocalDate(2026, 1, 31))
            .map { it.date }
        assertEquals(
            listOf(LocalDate(2026, 1, 1), LocalDate(2026, 1, 2), LocalDate(2026, 1, 3)),
            dates,
        )
    }

    @Test
    fun monthly_byMonthDay() {
        val event = BasicKalendarSyncEvent(
            date = LocalDate(2026, 1, 15),
            eventName = "Mid-month",
            recurrenceRule = KalendarRule(
                frequency = KalendarRecurrenceFrequency.MONTHLY,
                byMonthDay = listOf(15),
                count = 3,
            ),
        )
        val dates = event.expandOccurrences(LocalDate(2026, 1, 1), LocalDate(2026, 12, 31))
            .map { it.date }
        assertEquals(
            listOf(
                LocalDate(2026, 1, 15),
                LocalDate(2026, 2, 15),
                LocalDate(2026, 3, 15),
            ),
            dates,
        )
    }

    @Test
    fun yearly_byMonth() {
        val event = BasicKalendarSyncEvent(
            date = LocalDate(2024, 6, 1),
            eventName = "Annual June",
            recurrenceRule = KalendarRule(
                frequency = KalendarRecurrenceFrequency.YEARLY,
                byMonth = listOf(6),
                count = 3,
            ),
        )
        val dates = event.expandOccurrences(LocalDate(2024, 1, 1), LocalDate(2030, 12, 31))
            .map { it.date }
        assertEquals(
            listOf(
                LocalDate(2024, 6, 1),
                LocalDate(2025, 6, 1),
                LocalDate(2026, 6, 1),
            ),
            dates,
        )
    }

    @Test
    fun intervalZero_isRejectedByRule() {
        // KalendarRule init requires interval >= 1; expander also coerces as defense-in-depth.
        assertFailsWith<IllegalArgumentException> {
            KalendarRule(
                frequency = KalendarRecurrenceFrequency.DAILY,
                interval = 0,
                count = 5,
            )
        }
    }

    @Test
    fun expandedOccurrence_clearsId() {
        val event = BasicKalendarSyncEvent(
            id = "template-id",
            date = LocalDate(2026, 1, 1),
            eventName = "Daily",
            recurrenceRule = KalendarRule(
                frequency = KalendarRecurrenceFrequency.DAILY,
                count = 2,
            ),
        )
        val occurrences = event.expandOccurrences(LocalDate(2026, 1, 1), LocalDate(2026, 1, 31))
        assertTrue(occurrences.all { it.id == null })
        assertTrue(occurrences.all { it.eventName == "Daily" })
    }

    @Test
    fun emptyRange_returnsEmpty() {
        val event = BasicKalendarSyncEvent(
            date = LocalDate(2026, 6, 15),
            eventName = "X",
            recurrenceRule = KalendarRule(frequency = KalendarRecurrenceFrequency.DAILY, count = 10),
        )
        // window completely before start
        assertTrue(
            event.expandOccurrences(LocalDate(2026, 1, 1), LocalDate(2026, 1, 31)).isEmpty()
        )
    }
}
