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

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * Represents a calendar event tied to a specific date.
 *
 * Implementing classes may optionally provide [startTime] and [endTime] to enable
 * time-based ordering within a day, and [eventColor] to render a per-event colour dot.
 * All three properties default to `null` so that existing implementations remain
 * source-compatible without change.
 */
@Stable
interface KalendarEvent {
    /**
     * The date on which this event falls.
     */
    val date: LocalDate

    /**
     * A short, human-readable name for the event (e.g. "Team standup").
     */
    val eventName: String

    /**
     * An optional longer description for the event. Defaults to `null`.
     */
    val eventDescription: String?

    /**
     * The date and time at which the event begins. When non-null, events on the same
     * day are sorted by this value in ascending order (e.g. in the Agenda view).
     * Defaults to `null`.
     */
    val startTime: LocalDateTime? get() = null

    /**
     * The date and time at which the event ends. Displayed alongside [startTime] wherever
     * duration information is shown (e.g. in the Agenda view). Defaults to `null`.
     */
    val endTime: LocalDateTime? get() = null

    /**
     * An optional colour used to tint the event's indicator dot on the day cell.
     * When `null`, the calendar falls back to [com.himanshoe.kalendar.foundation.component.config.KalendarDayConfig.indicatorColor].
     * Defaults to `null`.
     */
    val eventColor: Color? get() = null
}

/**
 * A ready-to-use implementation of [KalendarEvent].
 *
 * @property date The date on which the event falls.
 * @property eventName A short, human-readable name for the event.
 * @property eventDescription An optional longer description. Defaults to `null`.
 * @property startTime Optional start date-time used for within-day ordering. Defaults to `null`.
 * @property endTime Optional end date-time shown alongside [startTime]. Defaults to `null`.
 * @property eventColor Optional colour for the event's indicator dot. When `null` the calendar
 *   falls back to the configured [com.himanshoe.kalendar.foundation.component.config.KalendarDayConfig.indicatorColor].
 *   Defaults to `null`.
 */
@Immutable
data class BasicKalendarEvent(
    override val date: LocalDate,
    override val eventName: String,
    override val eventDescription: String? = null,
    override val startTime: LocalDateTime? = null,
    override val endTime: LocalDateTime? = null,
    override val eventColor: Color? = null,
) : KalendarEvent

/**
 * A list of [KalendarEvent] instances.
 *
 * Use standard Kotlin collection builders to create one:
 * ```kotlin
 * val events: KalendarEvents = listOf(
 *     BasicKalendarEvent(
 *         date = today,
 *         eventName = "Team standup",
 *         startTime = LocalDateTime(today, LocalTime(9, 0)),
 *     ),
 * )
 * ```
 */
typealias KalendarEvents = List<KalendarEvent>
