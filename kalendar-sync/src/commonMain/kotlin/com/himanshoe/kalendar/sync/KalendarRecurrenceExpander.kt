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

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/**
 * Expands this recurring event into all concrete occurrences within the closed date range
 * [[rangeStart], [rangeEnd]], respecting the event's [KalendarSyncEvent.recurrenceRule].
 *
 * Supports all four [KalendarRecurrenceFrequency] values and the BYDAY, BYMONTHDAY, and BYMONTH
 * filters from RFC 5545. Both UNTIL and COUNT termination conditions are honoured.
 *
 * - Events without a [KalendarRule] are treated as single-occurrence: `this` is returned in a
 *   one-element list when its date is within the range, or an empty list otherwise.
 * - Each generated occurrence is a copy of the receiver with [KalendarSyncEvent.date] replaced by
 *   the occurrence date and [KalendarSyncEvent.id] cleared.
 *
 * ```kotlin
 * val template = BasicKalendarSyncEvent(
 *     date = LocalDate(2026, 1, 6),
 *     eventName = "Weekly team sync",
 *     recurrenceRule = KalendarRule(
 *         frequency = KalendarRecurrenceFrequency.WEEKLY,
 *         byDay = listOf(KalendarWeekDay.MONDAY),
 *     ),
 * )
 * val occurrences = template.expandOccurrences(
 *     rangeStart = LocalDate(2026, 1, 1),
 *     rangeEnd   = LocalDate(2026, 3, 31),
 * )
 * ```
 *
 * @param rangeStart Start of the expansion window (inclusive).
 * @param rangeEnd End of the expansion window (inclusive).
 */
fun KalendarSyncEvent.expandOccurrences(
    rangeStart: LocalDate,
    rangeEnd: LocalDate,
): List<KalendarSyncEvent> {
    val rule = recurrenceRule
        ?: return if (date in rangeStart..rangeEnd) listOf(this) else emptyList()

    val occurrences = mutableListOf<KalendarSyncEvent>()
    var occurrenceCount = 0
    var current = date

    while (true) {
        if (rule.count != null && occurrenceCount >= rule.count) break
        if (rule.until != null && current > rule.until) break
        if (current > rangeEnd) break

        if (current >= rangeStart && current.matchesFilters(rule)) {
            occurrences += copyWithDate(current)
            occurrenceCount++
        }

        current = current.advance(rule)
    }
    return occurrences
}

/**
 * Returns `true` if this date satisfies all BYMONTH, BYMONTHDAY, and BYDAY constraints in [rule].
 */
private fun LocalDate.matchesFilters(rule: KalendarRule): Boolean {
    if (rule.byMonth.isNotEmpty() && monthNumber !in rule.byMonth) return false
    if (rule.byMonthDay.isNotEmpty() && dayOfMonth !in rule.byMonthDay) return false
    if (rule.byDay.isNotEmpty() && dayOfWeek.toKalendarWeekDay() !in rule.byDay) return false
    return true
}

/**
 * Returns the next candidate date after this one, based on [rule]'s frequency and BYDAY strategy.
 *
 * WEEKLY rules with a non-empty BYDAY filter advance one day at a time so that every qualifying
 * day within each week is tested. All other combinations advance by one full interval unit.
 */
private fun LocalDate.advance(rule: KalendarRule): LocalDate =
    when (rule.frequency) {
        KalendarRecurrenceFrequency.DAILY ->
            plus(rule.interval, DateTimeUnit.DAY)

        KalendarRecurrenceFrequency.WEEKLY ->
            if (rule.byDay.isEmpty()) plus(rule.interval * 7, DateTimeUnit.DAY)
            else plus(1, DateTimeUnit.DAY)

        KalendarRecurrenceFrequency.MONTHLY ->
            plus(rule.interval, DateTimeUnit.MONTH)

        KalendarRecurrenceFrequency.YEARLY ->
            plus(rule.interval, DateTimeUnit.YEAR)
    }

/**
 * Maps a [DayOfWeek] to the corresponding [KalendarWeekDay] entry.
 */
private fun DayOfWeek.toKalendarWeekDay(): KalendarWeekDay = when (this) {
    DayOfWeek.MONDAY -> KalendarWeekDay.MONDAY
    DayOfWeek.TUESDAY -> KalendarWeekDay.TUESDAY
    DayOfWeek.WEDNESDAY -> KalendarWeekDay.WEDNESDAY
    DayOfWeek.THURSDAY -> KalendarWeekDay.THURSDAY
    DayOfWeek.FRIDAY -> KalendarWeekDay.FRIDAY
    DayOfWeek.SATURDAY -> KalendarWeekDay.SATURDAY
    else -> KalendarWeekDay.SUNDAY
}

/**
 * Returns a copy of this event with [KalendarSyncEvent.date] replaced by [date] and
 * [KalendarSyncEvent.id] cleared — representing a single materialised occurrence.
 */
private fun KalendarSyncEvent.copyWithDate(date: LocalDate): KalendarSyncEvent =
    BasicKalendarSyncEvent(
        id = null,
        date = date,
        eventName = eventName,
        eventDescription = eventDescription,
        startTime = startTime,
        endTime = endTime,
        recurrenceRule = recurrenceRule,
    )
