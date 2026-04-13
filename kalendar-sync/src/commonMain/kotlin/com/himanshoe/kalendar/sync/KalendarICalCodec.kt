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
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

/**
 * Pure Kotlin iCalendar (RFC 5545) encoder and decoder.
 * No platform APIs — works on Android, iOS, Desktop, and wasmJS.
 */
internal object KalendarICalCodec {

    /**
     * Encodes a list of [KalendarSyncEvent] into a complete iCalendar (.ics) string.
     */
    fun encode(events: List<KalendarSyncEvent>): String = buildString {
        appendLine("BEGIN:VCALENDAR")
        appendLine("VERSION:2.0")
        appendLine("PRODID:-//Kalendar//KalendarSync//EN")
        appendLine("CALSCALE:GREGORIAN")
        appendLine("METHOD:PUBLISH")
        for (event in events) {
            encodeEvent(event)
        }
        append("END:VCALENDAR")
    }

    private fun StringBuilder.encodeEvent(event: KalendarSyncEvent) {
        appendLine("BEGIN:VEVENT")
        appendLine("UID:${event.id ?: generateUid(event)}")
        appendLine("SUMMARY:${icalEscape(event.eventName)}")
        event.eventDescription?.let { appendLine("DESCRIPTION:${icalEscape(it)}") }

        if (event.isAllDay) {
            appendLine("DTSTART;VALUE=DATE:${event.date.toIcalDate()}")
            appendLine("DTEND;VALUE=DATE:${event.date.plusDays(1).toIcalDate()}")
        } else {
            val startTime = event.startTime ?: LocalTime(0, 0)
            val endTime = event.endTime ?: LocalTime(23, 59)
            appendLine("DTSTART:${LocalDateTime(event.date, startTime).toIcalDateTime()}")
            appendLine("DTEND:${LocalDateTime(event.date, endTime).toIcalDateTime()}")
        }
        event.recurrenceRule?.let { appendLine("RRULE:${it.toIcal()}") }
        appendLine("END:VEVENT")
    }

    /**
     * Parses an iCalendar (.ics) string and returns the contained events as
     * [BasicKalendarSyncEvent] instances.
     */
    fun decode(icsContent: String): List<KalendarSyncEvent> {
        val events = mutableListOf<KalendarSyncEvent>()
        val lines = unfoldLines(icsContent)

        var inEvent = false
        var uid: String? = null
        var summary: String? = null
        var description: String? = null
        var dtStart: String? = null
        var dtEnd: String? = null
        var isAllDay = false
        var rruleRaw: String? = null

        for (line in lines) {
            when {
                line == "BEGIN:VEVENT" -> {
                    inEvent = true
                    uid = null; summary = null; description = null
                    dtStart = null; dtEnd = null; isAllDay = false; rruleRaw = null
                }
                line == "END:VEVENT" && inEvent -> {
                    inEvent = false
                    val startRaw = dtStart ?: continue
                    val date = parseIcalDate(startRaw) ?: continue
                    val (startTime, endTime) = if (isAllDay) {
                        null to null
                    } else {
                        parseIcalTime(startRaw) to dtEnd?.let { parseIcalTime(it) }
                    }
                    events += BasicKalendarSyncEvent(
                        id = uid,
                        date = date,
                        eventName = icalUnescape(summary ?: ""),
                        eventDescription = description?.let { icalUnescape(it) },
                        startTime = startTime,
                        endTime = endTime,
                        recurrenceRule = rruleRaw?.let { parseRRule(it) },
                    )
                }
                inEvent -> parseLine(line)?.let { (key, params, value) ->
                    when (key) {
                        "UID" -> uid = value
                        "SUMMARY" -> summary = value
                        "DESCRIPTION" -> description = value
                        "DTSTART" -> {
                            dtStart = value
                            isAllDay = params.contains("VALUE=DATE")
                        }
                        "DTEND" -> dtEnd = value
                        "RRULE" -> rruleRaw = value
                    }
                }
            }
        }
        return events
    }

    /**
     * Unfolds RFC 5545 line continuations (CRLF or LF followed by whitespace)
     * into single logical lines, then strips blank lines.
     */
    private fun unfoldLines(raw: String): List<String> {
        return raw.replace("\r\n ", "").replace("\r\n\t", "")
            .replace("\n ", "").replace("\n\t", "")
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    /**
     * Parses a single iCalendar property line in `KEY;PARAM=VAL:value` form into a
     * `(key, params, value)` triple. Returns `null` if the line has no colon separator.
     */
    private fun parseLine(line: String): Triple<String, Set<String>, String>? {
        val colonIdx = line.indexOf(':')
        if (colonIdx < 0) return null
        val keyPart = line.substring(0, colonIdx)
        val value = line.substring(colonIdx + 1)
        val parts = keyPart.split(';')
        val key = parts.first()
        val params = parts.drop(1).toSet()
        return Triple(key, params, value)
    }

    private fun LocalDate.toIcalDate(): String =
        "%04d%02d%02d".format(year, monthNumber, dayOfMonth)

    private fun LocalDateTime.toIcalDateTime(): String =
        "%04d%02d%02dT%02d%02d%02dZ".format(
            date.year, date.monthNumber, date.dayOfMonth,
            hour, minute, second
        )

    private fun parseIcalDate(value: String): LocalDate? = runCatching {
        val v = value.substringBefore('T').filter { it.isDigit() }
        if (v.length < 8) return null
        LocalDate(v.substring(0, 4).toInt(), v.substring(4, 6).toInt(), v.substring(6, 8).toInt())
    }.getOrNull()

    private fun parseIcalTime(value: String): LocalTime? = runCatching {
        val timePart = value.substringAfter('T', "").trimEnd('Z')
        if (timePart.length < 6) return null
        LocalTime(timePart.substring(0, 2).toInt(), timePart.substring(2, 4).toInt(), timePart.substring(4, 6).toInt())
    }.getOrNull()

    private fun LocalDate.plusDays(days: Int): LocalDate {
        var result = this
        repeat(days) { result = result.nextDay() }
        return result
    }

    private fun LocalDate.nextDay(): LocalDate {
        val maxDay = daysInMonth(year, monthNumber)
        return if (dayOfMonth < maxDay) {
            LocalDate(year, monthNumber, dayOfMonth + 1)
        } else if (monthNumber < 12) {
            LocalDate(year, monthNumber + 1, 1)
        } else {
            LocalDate(year + 1, 1, 1)
        }
    }

    private fun daysInMonth(year: Int, month: Int): Int {
        val days = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        if (month == 2 && isLeapYear(year)) return 29
        return days[month - 1]
    }

    private fun isLeapYear(year: Int) =
        (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

    private fun icalEscape(text: String): String =
        text.replace("\\", "\\\\").replace(";", "\\;").replace(",", "\\,").replace("\n", "\\n")

    private fun icalUnescape(text: String): String =
        text.replace("\\n", "\n").replace("\\,", ",").replace("\\;", ";").replace("\\\\", "\\")

    private fun generateUid(event: KalendarSyncEvent): String =
        "${event.date}-${event.eventName.hashCode().toUInt()}-kalendar@himanshoe.com"

    /**
     * Serialises a [KalendarRule] to the value portion of an RFC 5545 RRULE property,
     * e.g. `FREQ=WEEKLY;INTERVAL=2;BYDAY=MO,WE,FR`.
     */
    private fun KalendarRule.toIcal(): String = buildString {
        append("FREQ=${frequency.name}")
        if (interval != 1) append(";INTERVAL=$interval")
        count?.let { append(";COUNT=$it") }
        until?.let { append(";UNTIL=${it.toIcalDate()}") }
        if (byDay.isNotEmpty()) append(";BYDAY=${byDay.joinToString(",") { it.toIcalString() }}")
        if (byMonthDay.isNotEmpty()) append(";BYMONTHDAY=${byMonthDay.joinToString(",")}")
        if (byMonth.isNotEmpty()) append(";BYMONTH=${byMonth.joinToString(",")}")
    }

    /**
     * Parses the value portion of an RFC 5545 RRULE property into a [KalendarRule].
     * Returns `null` if the FREQ component is missing or unrecognised.
     */
    private fun parseRRule(value: String): KalendarRule? = runCatching {
        val parts = value.split(";").mapNotNull { segment ->
            val eq = segment.indexOf('=')
            if (eq < 0) null else segment.substring(0, eq) to segment.substring(eq + 1)
        }.toMap()

        val frequency = when (parts["FREQ"]) {
            "DAILY" -> KalendarRecurrenceFrequency.DAILY
            "WEEKLY" -> KalendarRecurrenceFrequency.WEEKLY
            "MONTHLY" -> KalendarRecurrenceFrequency.MONTHLY
            "YEARLY" -> KalendarRecurrenceFrequency.YEARLY
            else -> return null
        }
        KalendarRule(
            frequency = frequency,
            interval = parts["INTERVAL"]?.toIntOrNull() ?: 1,
            count = parts["COUNT"]?.toIntOrNull(),
            until = parts["UNTIL"]?.let { parseIcalDate(it) },
            byDay = parts["BYDAY"]
                ?.split(",")
                ?.mapNotNull { it.trim().toKalendarWeekDay() }
                ?: emptyList(),
            byMonthDay = parts["BYMONTHDAY"]
                ?.split(",")
                ?.mapNotNull { it.trim().toIntOrNull() }
                ?: emptyList(),
            byMonth = parts["BYMONTH"]
                ?.split(",")
                ?.mapNotNull { it.trim().toIntOrNull() }
                ?: emptyList(),
        )
    }.getOrNull()

    private fun KalendarWeekDay.toIcalString(): String = when (this) {
        KalendarWeekDay.MONDAY -> "MO"
        KalendarWeekDay.TUESDAY -> "TU"
        KalendarWeekDay.WEDNESDAY -> "WE"
        KalendarWeekDay.THURSDAY -> "TH"
        KalendarWeekDay.FRIDAY -> "FR"
        KalendarWeekDay.SATURDAY -> "SA"
        KalendarWeekDay.SUNDAY -> "SU"
    }

    private fun String.toKalendarWeekDay(): KalendarWeekDay? = when (this) {
        "MO" -> KalendarWeekDay.MONDAY
        "TU" -> KalendarWeekDay.TUESDAY
        "WE" -> KalendarWeekDay.WEDNESDAY
        "TH" -> KalendarWeekDay.THURSDAY
        "FR" -> KalendarWeekDay.FRIDAY
        "SA" -> KalendarWeekDay.SATURDAY
        "SU" -> KalendarWeekDay.SUNDAY
        else -> null
    }
}
