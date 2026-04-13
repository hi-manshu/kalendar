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

package com.himanshoe.kalendar.foundation.event

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KalendarEventsTest {

    private val date1 = LocalDate(2026, 6, 10)
    private val date2 = LocalDate(2026, 6, 20)

    @Test
    fun emptyEvents_defaultsToEmptyList() {
        val events: KalendarEvents = emptyList()
        assertTrue(events.isEmpty())
    }

    @Test
    fun eventsOnDate_returnsMatchingEvents() {
        val event1 = BasicKalendarEvent(date = date1, eventName = "Meeting", eventDescription = null)
        val event2 = BasicKalendarEvent(date = date2, eventName = "Lunch", eventDescription = "With team")
        val events: KalendarEvents = listOf(event1, event2)

        val onDate1 = events.filter { it.date == date1 }
        assertEquals(1, onDate1.size)
        assertEquals("Meeting", onDate1.first().eventName)
    }

    @Test
    fun eventsOnDate_noMatch_returnsEmpty() {
        val event = BasicKalendarEvent(date = date1, eventName = "Meeting", eventDescription = null)
        val events: KalendarEvents = listOf(event)

        val onDate2 = events.filter { it.date == date2 }
        assertTrue(onDate2.isEmpty())
    }

    @Test
    fun multipleEventsOnSameDate() {
        val e1 = BasicKalendarEvent(date = date1, eventName = "Morning standup", eventDescription = null)
        val e2 = BasicKalendarEvent(date = date1, eventName = "Retro", eventDescription = "End of sprint")
        val events: KalendarEvents = listOf(e1, e2)

        val onDate = events.filter { it.date == date1 }
        assertEquals(2, onDate.size)
    }

    @Test
    fun basicKalendarEvent_equality() {
        val a = BasicKalendarEvent(date = date1, eventName = "A", eventDescription = "desc")
        val b = BasicKalendarEvent(date = date1, eventName = "A", eventDescription = "desc")
        assertEquals(a, b)
    }

    @Test
    fun basicKalendarEvent_implementsKalendarEvent() {
        val event: KalendarEvent = BasicKalendarEvent(
            date = date1,
            eventName = "Test",
            eventDescription = null,
        )
        assertEquals(date1, event.date)
        assertEquals("Test", event.eventName)
    }
}
