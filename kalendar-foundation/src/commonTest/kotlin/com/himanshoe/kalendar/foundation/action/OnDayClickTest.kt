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

import com.himanshoe.kalendar.foundation.event.BasicKalendarEvent
import com.himanshoe.kalendar.foundation.event.KalendarEvent
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OnDayClickTest {

    private val d1 = LocalDate(2026, 6, 10)
    private val d2 = LocalDate(2026, 6, 15)
    private val d3 = LocalDate(2026, 6, 20)

    private val allEvents = listOf(
        BasicKalendarEvent(date = d1, eventName = "A"),
        BasicKalendarEvent(date = d2, eventName = "B"),
        BasicKalendarEvent(date = d3, eventName = "C"),
        BasicKalendarEvent(date = d2, eventName = "B2"),
    )

    @Test
    fun single_invokesCallbackAndUpdatesDate() {
        var clicked: LocalDate? = null
        var callbackDate: LocalDate? = null
        var callbackEvents: List<KalendarEvent> = emptyList()

        d2.onDayClick(
            events = allEvents.filter { it.date == d2 },
            allEvents = allEvents,
            rangeStartDate = null,
            rangeEndDate = null,
            onDaySelectionAction = OnDaySelectionAction.Single { date, events ->
                callbackDate = date
                callbackEvents = events
            },
            onClickedNewDate = { clicked = it },
            onMultipleClickedNewDate = {},
            onClickedRangeStartDate = {},
            onClickedRangeEndDate = {},
            onUpdateSelectedRange = {},
        )

        assertEquals(d2, clicked)
        assertEquals(d2, callbackDate)
        assertEquals(2, callbackEvents.size)
    }

    @Test
    fun multiple_togglesViaHostCallback() {
        val selected = mutableListOf<LocalDate>()
        var lastTapped: LocalDate? = null

        d1.onDayClick(
            events = emptyList(),
            rangeStartDate = null,
            rangeEndDate = null,
            onDaySelectionAction = OnDaySelectionAction.Multiple { date, _ -> lastTapped = date },
            onClickedNewDate = {},
            onMultipleClickedNewDate = { date ->
                if (selected.contains(date)) selected.remove(date) else selected.add(date)
            },
            onClickedRangeStartDate = {},
            onClickedRangeEndDate = {},
            onUpdateSelectedRange = {},
        )
        d2.onDayClick(
            events = emptyList(),
            rangeStartDate = null,
            rangeEndDate = null,
            onDaySelectionAction = OnDaySelectionAction.Multiple { date, _ -> lastTapped = date },
            onClickedNewDate = {},
            onMultipleClickedNewDate = { date ->
                if (selected.contains(date)) selected.remove(date) else selected.add(date)
            },
            onClickedRangeStartDate = {},
            onClickedRangeEndDate = {},
            onUpdateSelectedRange = {},
        )
        // toggle d1 off
        d1.onDayClick(
            events = emptyList(),
            rangeStartDate = null,
            rangeEndDate = null,
            onDaySelectionAction = OnDaySelectionAction.Multiple { date, _ -> lastTapped = date },
            onClickedNewDate = {},
            onMultipleClickedNewDate = { date ->
                if (selected.contains(date)) selected.remove(date) else selected.add(date)
            },
            onClickedRangeStartDate = {},
            onClickedRangeEndDate = {},
            onUpdateSelectedRange = {},
        )

        assertEquals(listOf(d2), selected)
        assertEquals(d1, lastTapped)
    }

    @Test
    fun range_firstClickSetsStartOnly() {
        var start: LocalDate? = null
        var end: LocalDate? = null
        var range: KalendarSelectedDayRange? = KalendarSelectedDayRange(d1, d3) // previous range
        var rangeCallbackInvoked = false

        d2.onDayClick(
            events = emptyList(),
            allEvents = allEvents,
            rangeStartDate = null,
            rangeEndDate = null,
            onDaySelectionAction = OnDaySelectionAction.Range { _, _ -> rangeCallbackInvoked = true },
            onClickedNewDate = {},
            onMultipleClickedNewDate = {},
            onClickedRangeStartDate = { start = it },
            onClickedRangeEndDate = { end = it },
            onUpdateSelectedRange = { range = it },
        )

        assertEquals(d2, start)
        assertNull(end)
        assertNull(range)
        assertTrue(!rangeCallbackInvoked)
    }

    @Test
    fun range_secondClickCompletesAndSwapsIfNeeded() {
        var start: LocalDate? = d3
        var end: LocalDate? = null
        var range: KalendarSelectedDayRange? = null
        var eventsInRange: List<KalendarEvent> = emptyList()

        // tap earlier day second — should swap
        d1.onDayClick(
            events = allEvents.filter { it.date == d1 },
            allEvents = allEvents,
            rangeStartDate = start,
            rangeEndDate = end,
            onDaySelectionAction = OnDaySelectionAction.Range { r, events ->
                range = r
                eventsInRange = events
            },
            onClickedNewDate = {},
            onMultipleClickedNewDate = {},
            onClickedRangeStartDate = { start = it },
            onClickedRangeEndDate = { end = it },
            onUpdateSelectedRange = { range = it },
        )

        assertEquals(d1, range!!.start)
        assertEquals(d3, range!!.endInclusive)
        assertEquals(d1, start)
        assertEquals(d3, end)
        // events on d1, d2, d3 (4 total including B2 on d2)
        assertEquals(4, eventsInRange.size)
        assertTrue(eventsInRange.all { it.date in range!! })
    }

    @Test
    fun range_thirdClickRestartsRange() {
        var start: LocalDate? = d1
        var end: LocalDate? = d3
        var range: KalendarSelectedDayRange? = KalendarSelectedDayRange(d1, d3)

        d2.onDayClick(
            events = emptyList(),
            allEvents = allEvents,
            rangeStartDate = start,
            rangeEndDate = end,
            onDaySelectionAction = OnDaySelectionAction.Range { _, _ -> },
            onClickedNewDate = {},
            onMultipleClickedNewDate = {},
            onClickedRangeStartDate = { start = it },
            onClickedRangeEndDate = { end = it },
            onUpdateSelectedRange = { range = it },
        )

        assertEquals(d2, start)
        assertNull(end)
        assertNull(range)
    }

    @Test
    fun noOp_stillUpdatesSingleDateViaSingleImpl() {
        var clicked: LocalDate? = null
        d1.onDayClick(
            events = emptyList(),
            rangeStartDate = null,
            rangeEndDate = null,
            onDaySelectionAction = OnDaySelectionAction.NoOp,
            onClickedNewDate = { clicked = it },
            onMultipleClickedNewDate = {},
            onClickedRangeStartDate = {},
            onClickedRangeEndDate = {},
            onUpdateSelectedRange = {},
        )
        assertEquals(d1, clicked)
    }

    @Test
    fun disallowed_date_is_ignored() {
        var clicked: LocalDate? = null
        var callbackFired = false
        d1.onDayClick(
            events = emptyList(),
            rangeStartDate = null,
            rangeEndDate = null,
            onDaySelectionAction = OnDaySelectionAction.Single { _, _ -> callbackFired = true },
            onClickedNewDate = { clicked = it },
            onMultipleClickedNewDate = {},
            onClickedRangeStartDate = {},
            onClickedRangeEndDate = {},
            onUpdateSelectedRange = {},
            isDateAllowed = { false },
        )
        assertNull(clicked)
        assertFalse(callbackFired)
    }

    @Test
    fun range_with_disallowed_middle_is_refused() {
        var range: KalendarSelectedDayRange? = null
        var rangeCallback = false
        d3.onDayClick(
            events = emptyList(),
            allEvents = allEvents,
            rangeStartDate = d1,
            rangeEndDate = null,
            onDaySelectionAction = OnDaySelectionAction.Range { _, _ -> rangeCallback = true },
            onClickedNewDate = {},
            onMultipleClickedNewDate = {},
            onClickedRangeStartDate = {},
            onClickedRangeEndDate = {},
            onUpdateSelectedRange = { range = it },
            isDateAllowed = { it != d2 },
        )
        assertNull(range)
        assertFalse(rangeCallback)
    }
}
