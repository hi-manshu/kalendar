/*
 *
 *  * Copyright 2025 Kalendar Contributors (https://www.himanshoe.com). All rights reserved.
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

import com.himanshoe.kalendar.foundation.color.KalendarColor
import kotlinx.datetime.LocalDate

/**
 * Represents an event in the calendar.
 */
interface KalenderEvent {
    /**
     * The date of the event.
     */
    val date: LocalDate

    /**
     * The name of the event.
     */
    val eventName: String

    /**
     * The description of the event.
     */
    val eventDescription: String?

    /**
     * Optional color for this event's indicator. When null, falls back to config-level indicatorColor.
     */
    val eventColor: KalendarColor?
        get() = null
}

/**
 * A basic implementation of the [KalenderEvent] interface.
 *
 * @property date The date of the event.
 * @property eventName The name of the event.
 * @property eventDescription The description of the event.
 * @property eventColor Optional per-event indicator color.
 */
data class BasicKalendarEvent(
    override val date: LocalDate,
    override val eventName: String,
    override val eventDescription: String?,
    override val eventColor: KalendarColor? = null,
) : KalenderEvent

/**
 * A collection of calendar events.
 *
 * @property eventList The list of events.
 */
data class KalendarEvents(
    val eventList: List<KalenderEvent> = emptyList()
)
