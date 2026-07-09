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
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class KalendarICalCodecTest {

    @Test
    fun exportToIcs_containsCalendarEnvelope() {
        val ics = exportToIcs(
            listOf(
                BasicKalendarSyncEvent(
                    date = LocalDate(2026, 6, 15),
                    eventName = "Demo",
                )
            )
        )
        assertTrue(ics.contains("BEGIN:VCALENDAR"))
        assertTrue(ics.contains("END:VCALENDAR"))
        assertTrue(ics.contains("BEGIN:VEVENT"))
        assertTrue(ics.contains("END:VEVENT"))
        assertTrue(ics.contains("SUMMARY:Demo"))
    }

    @Test
    fun exportToIcs_timedEvent_usesFloatingLocalTimeWithoutZ() {
        val ics = exportToIcs(
            listOf(
                BasicKalendarSyncEvent(
                    date = LocalDate(2026, 6, 15),
                    eventName = "Standup",
                    startTime = LocalTime(9, 30),
                    endTime = LocalTime(10, 0),
                )
            )
        )
        assertTrue(ics.contains("DTSTART:20260615T093000"))
        assertTrue(ics.contains("DTEND:20260615T100000"))
        assertFalse(ics.contains("DTSTART:20260615T093000Z"))
    }

    @Test
    fun exportToIcs_allDayEvent_usesValueDate() {
        val ics = exportToIcs(
            listOf(
                BasicKalendarSyncEvent(
                    date = LocalDate(2026, 6, 15),
                    eventName = "Holiday",
                    // all-day when no start/end times
                )
            )
        )
        assertTrue(ics.contains("DTSTART;VALUE=DATE:20260615"))
        assertTrue(ics.contains("DTEND;VALUE=DATE:20260616"))
    }

    @Test
    fun exportToIcs_escapesSpecialCharacters() {
        val ics = exportToIcs(
            listOf(
                BasicKalendarSyncEvent(
                    date = LocalDate(2026, 1, 1),
                    eventName = "Meet; Plan, Discuss",
                    eventDescription = "Line1\nLine2",
                )
            )
        )
        assertTrue(ics.contains("SUMMARY:Meet\\; Plan\\, Discuss"))
        assertTrue(ics.contains("DESCRIPTION:Line1\\nLine2"))
    }

    @Test
    fun exportToIcs_includesRrule() {
        val ics = exportToIcs(
            listOf(
                BasicKalendarSyncEvent(
                    date = LocalDate(2026, 1, 5),
                    eventName = "Weekly",
                    recurrenceRule = KalendarRule(
                        frequency = KalendarRecurrenceFrequency.WEEKLY,
                        interval = 2,
                        byDay = listOf(KalendarWeekDay.MONDAY, KalendarWeekDay.WEDNESDAY),
                    ),
                )
            )
        )
        assertTrue(ics.contains("RRULE:FREQ=WEEKLY;INTERVAL=2;BYDAY=MO,WE"))
    }

    @Test
    fun importFromIcs_roundTrip_preservesCoreFields() {
        val original = BasicKalendarSyncEvent(
            id = "uid-123",
            date = LocalDate(2026, 3, 10),
            eventName = "Round trip",
            eventDescription = "Desc",
            startTime = LocalTime(14, 0),
            endTime = LocalTime(15, 30),
        )
        val imported = importFromIcs(exportToIcs(listOf(original)))
        assertEquals(1, imported.size)
        val event = imported.first()
        assertEquals("uid-123", event.id)
        assertEquals(LocalDate(2026, 3, 10), event.date)
        assertEquals("Round trip", event.eventName)
        assertEquals("Desc", event.eventDescription)
        assertEquals(LocalTime(14, 0), event.startTime)
        assertEquals(LocalTime(15, 30), event.endTime)
    }

    @Test
    fun importFromIcs_parsesRrule() {
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            BEGIN:VEVENT
            UID:r1
            SUMMARY:Daily
            DTSTART;VALUE=DATE:20260101
            DTEND;VALUE=DATE:20260102
            RRULE:FREQ=DAILY;COUNT=5
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val events = importFromIcs(ics)
        assertEquals(1, events.size)
        val rule = events.first().recurrenceRule
        assertNotNull(rule)
        assertEquals(KalendarRecurrenceFrequency.DAILY, rule.frequency)
        assertEquals(5, rule.count)
    }

    @Test
    fun importFromIcs_unfoldsContinuedLines() {
        val ics = "BEGIN:VCALENDAR\r\nVERSION:2.0\r\nBEGIN:VEVENT\r\nUID:u\r\nSUMMARY:Very long\r\n  title continued\r\nDTSTART;VALUE=DATE:20260601\r\nEND:VEVENT\r\nEND:VCALENDAR"
        val events = importFromIcs(ics)
        assertEquals(1, events.size)
        assertEquals("Very long title continued", events.first().eventName)
    }

    @Test
    fun importFromIcs_skipsEventWithoutDtStart() {
        val ics = """
            BEGIN:VCALENDAR
            BEGIN:VEVENT
            UID:x
            SUMMARY:No start
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()
        assertTrue(importFromIcs(ics).isEmpty())
    }

    @Test
    fun importFromIcs_emptyCalendar_returnsEmpty() {
        val ics = "BEGIN:VCALENDAR\nVERSION:2.0\nEND:VCALENDAR"
        assertTrue(importFromIcs(ics).isEmpty())
    }
}
