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

/**
 * How frequently a recurring event repeats, corresponding to the FREQ part of an RFC 5545 RRULE.
 */
enum class KalendarRecurrenceFrequency {
    /** Repeat every N days. */
    DAILY,

    /** Repeat every N weeks, optionally restricted to specific days via [KalendarRule.byDay]. */
    WEEKLY,

    /**
     * Repeat every N months, optionally restricted to specific days of the month via
     * [KalendarRule.byMonthDay].
     */
    MONTHLY,

    /**
     * Repeat every N years, optionally restricted to specific months via
     * [KalendarRule.byMonth].
     */
    YEARLY,
}

/**
 * A day-of-week value for use in the BYDAY clause of a [KalendarRule].
 *
 * Use these values to specify which days of the week a WEEKLY rule should repeat on:
 * ```kotlin
 * KalendarRule(
 *     frequency = KalendarRecurrenceFrequency.WEEKLY,
 *     byDay = listOf(KalendarWeekDay.MONDAY, KalendarWeekDay.WEDNESDAY, KalendarWeekDay.FRIDAY),
 * )
 * ```
 */
enum class KalendarWeekDay {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
}

/**
 * An RFC 5545 RRULE (recurrence rule) describing when a recurring event repeats.
 * Pass an instance to [KalendarSyncEvent.recurrenceRule]; the [KalendarRecurrenceExpander]
 * expands it into concrete [kotlinx.datetime.LocalDate] occurrences.
 *
 * ```kotlin
 * // Every weekday
 * KalendarRule(
 *     frequency = KalendarRecurrenceFrequency.WEEKLY,
 *     byDay = listOf(
 *         KalendarWeekDay.MONDAY, KalendarWeekDay.TUESDAY, KalendarWeekDay.WEDNESDAY,
 *         KalendarWeekDay.THURSDAY, KalendarWeekDay.FRIDAY,
 *     ),
 * )
 *
 * // Monthly on the 1st and 15th, ten times
 * KalendarRule(
 *     frequency = KalendarRecurrenceFrequency.MONTHLY,
 *     count = 10,
 *     byMonthDay = listOf(1, 15),
 * )
 * ```
 *
 * @param frequency How often the event repeats. Required.
 * @param interval How many [frequency] units between each occurrence. Must be ≥ 1. Defaults to 1.
 * @param count Total number of occurrences including the first. Mutually exclusive with [until].
 * @param until The last date on which an occurrence is allowed. Mutually exclusive with [count].
 * @param byDay For WEEKLY rules, restricts occurrences to these days of the week.
 *   May also be set for MONTHLY/YEARLY rules to select specific weekdays within the period.
 * @param byMonthDay For MONTHLY rules, restricts occurrences to these day-of-month values (1–31).
 * @param byMonth For YEARLY rules, restricts occurrences to these month values (1–12).
 */
data class KalendarRule(
    val frequency: KalendarRecurrenceFrequency,
    val interval: Int = 1,
    val count: Int? = null,
    val until: LocalDate? = null,
    val byDay: List<KalendarWeekDay> = emptyList(),
    val byMonthDay: List<Int> = emptyList(),
    val byMonth: List<Int> = emptyList(),
) {
    init {
        require(interval >= 1) { "interval must be >= 1, was $interval" }
        require(count == null || count >= 1) { "count must be >= 1, was $count" }
        require(count == null || until == null) { "count and until are mutually exclusive" }
    }
}
