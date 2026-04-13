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

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * Creates an Android [KalendarSyncProvider] backed by [CalendarContract].
 *
 * **Required permissions** — add to your `AndroidManifest.xml`:
 * ```xml
 * <uses-permission android:name="android.permission.READ_CALENDAR" />
 * <uses-permission android:name="android.permission.WRITE_CALENDAR" />
 * ```
 * Then request them at runtime with `ActivityCompat.requestPermissions` before
 * calling any sync function.
 *
 * @param context An application or activity context used to access the ContentResolver.
 */
fun KalendarSync(context: Context): KalendarSyncProvider = AndroidKalendarSync(context)

private class AndroidKalendarSync(private val context: Context) : KalendarSyncProvider {

    private val tz get() = TimeZone.currentSystemDefault()

    override suspend fun hasPermission(): Boolean {
        val read = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR)
        val write = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR)
        return read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED
    }

    override suspend fun fetchEvents(
        startDate: LocalDate,
        endDate: LocalDate,
    ): KalendarSyncResult<List<KalendarSyncEvent>> {
        if (!hasPermission()) return KalendarSyncResult.PermissionDenied
        return withContext(Dispatchers.IO) {
            runCatching {
                val startMillis = startDate.atStartOfDayIn(tz).toEpochMilliseconds()
                val endMillis = endDate.atStartOfDayIn(tz).toEpochMilliseconds() + DAY_MILLIS - 1

                val projection = arrayOf(
                    CalendarContract.Events._ID,
                    CalendarContract.Events.TITLE,
                    CalendarContract.Events.DESCRIPTION,
                    CalendarContract.Events.DTSTART,
                    CalendarContract.Events.DTEND,
                    CalendarContract.Events.ALL_DAY,
                )
                val selection =
                    "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ? " +
                        "AND ${CalendarContract.Events.DELETED} != 1"
                val selectionArgs = arrayOf(startMillis.toString(), endMillis.toString())

                val events = mutableListOf<KalendarSyncEvent>()
                context.contentResolver.query(
                    CalendarContract.Events.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    "${CalendarContract.Events.DTSTART} ASC",
                )?.use { cursor ->
                    val idIdx = cursor.getColumnIndex(CalendarContract.Events._ID)
                    val titleIdx = cursor.getColumnIndex(CalendarContract.Events.TITLE)
                    val descIdx = cursor.getColumnIndex(CalendarContract.Events.DESCRIPTION)
                    val startIdx = cursor.getColumnIndex(CalendarContract.Events.DTSTART)
                    val endIdx = cursor.getColumnIndex(CalendarContract.Events.DTEND)
                    val allDayIdx = cursor.getColumnIndex(CalendarContract.Events.ALL_DAY)

                    while (cursor.moveToNext()) {
                        val allDay = cursor.getInt(allDayIdx) != 0
                        val startMs = cursor.getLong(startIdx)
                        val endMs = cursor.getLong(endIdx)
                        val date = millisToLocalDate(startMs)
                        events += BasicKalendarSyncEvent(
                            id = cursor.getLong(idIdx).toString(),
                            date = date,
                            eventName = cursor.getString(titleIdx) ?: "",
                            eventDescription = cursor.getString(descIdx)?.takeIf { it.isNotBlank() },
                            startTime = if (allDay) null else millisToLocalTime(startMs),
                            endTime = if (allDay) null else millisToLocalTime(endMs),
                        )
                    }
                }
                KalendarSyncResult.Success(events)
            }.getOrElse { KalendarSyncResult.Error("Failed to fetch events", it) }
        }
    }

    override suspend fun insertEvent(event: KalendarSyncEvent): KalendarSyncResult<String> {
        if (!hasPermission()) return KalendarSyncResult.PermissionDenied
        return withContext(Dispatchers.IO) {
            runCatching {
                val calendarId = defaultCalendarId()
                    ?: return@withContext KalendarSyncResult.Error("No writable calendar found on device")

                val startMs = if (event.isAllDay) {
                    event.date.atStartOfDayIn(tz).toEpochMilliseconds()
                } else {
                    localDateTimeToMillis(event.date, event.startTime ?: LocalTime(0, 0))
                }
                val endMs = if (event.isAllDay) {
                    startMs + DAY_MILLIS
                } else {
                    localDateTimeToMillis(event.date, event.endTime ?: LocalTime(23, 59))
                }

                val values = ContentValues().apply {
                    put(CalendarContract.Events.CALENDAR_ID, calendarId)
                    put(CalendarContract.Events.TITLE, event.eventName)
                    put(CalendarContract.Events.DESCRIPTION, event.eventDescription ?: "")
                    put(CalendarContract.Events.DTSTART, startMs)
                    put(CalendarContract.Events.DTEND, endMs)
                    put(CalendarContract.Events.ALL_DAY, if (event.isAllDay) 1 else 0)
                    put(CalendarContract.Events.EVENT_TIMEZONE, tz.id)
                }

                val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
                    ?: return@withContext KalendarSyncResult.Error("Insert returned a null URI")
                KalendarSyncResult.Success(uri.lastPathSegment ?: "")
            }.getOrElse { KalendarSyncResult.Error("Failed to insert event", it) }
        }
    }

    override suspend fun updateEvent(eventId: String, event: KalendarSyncEvent): KalendarSyncResult<Unit> {
        if (!hasPermission()) return KalendarSyncResult.PermissionDenied
        return withContext(Dispatchers.IO) {
            runCatching {
                val startMs = if (event.isAllDay) {
                    event.date.atStartOfDayIn(tz).toEpochMilliseconds()
                } else {
                    localDateTimeToMillis(event.date, event.startTime ?: LocalTime(0, 0))
                }
                val endMs = if (event.isAllDay) {
                    startMs + DAY_MILLIS
                } else {
                    localDateTimeToMillis(event.date, event.endTime ?: LocalTime(23, 59))
                }
                val uri = ContentUris.withAppendedId(
                    CalendarContract.Events.CONTENT_URI,
                    eventId.toLong(),
                )
                val values = ContentValues().apply {
                    put(CalendarContract.Events.TITLE, event.eventName)
                    put(CalendarContract.Events.DESCRIPTION, event.eventDescription ?: "")
                    put(CalendarContract.Events.DTSTART, startMs)
                    put(CalendarContract.Events.DTEND, endMs)
                    put(CalendarContract.Events.ALL_DAY, if (event.isAllDay) 1 else 0)
                    put(CalendarContract.Events.EVENT_TIMEZONE, tz.id)
                }
                val updated = context.contentResolver.update(uri, values, null, null)
                if (updated > 0) KalendarSyncResult.Success(Unit)
                else KalendarSyncResult.Error("Event with id=$eventId not found")
            }.getOrElse { KalendarSyncResult.Error("Failed to update event", it) }
        }
    }

    override suspend fun deleteEvent(eventId: String): KalendarSyncResult<Unit> {
        if (!hasPermission()) return KalendarSyncResult.PermissionDenied
        return withContext(Dispatchers.IO) {
            runCatching {
                val uri = ContentUris.withAppendedId(
                    CalendarContract.Events.CONTENT_URI,
                    eventId.toLong(),
                )
                val deleted = context.contentResolver.delete(uri, null, null)
                if (deleted > 0) KalendarSyncResult.Success(Unit)
                else KalendarSyncResult.Error("Event with id=$eventId not found")
            }.getOrElse { KalendarSyncResult.Error("Failed to delete event", it) }
        }
    }

    /**
     * Queries [CalendarContract.Calendars] for the primary writable calendar ID,
     * falling back to any contributor-level calendar if no primary is flagged.
     * Returns `null` when no writable calendar exists on the device.
     */
    private fun defaultCalendarId(): Long? {
        val projection = arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.IS_PRIMARY)
        val selection = "${CalendarContract.Calendars.VISIBLE} = 1 AND " +
            "${CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL} >= ${CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR}"
        return context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            selection,
            null,
            "${CalendarContract.Calendars.IS_PRIMARY} DESC",
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getLong(cursor.getColumnIndex(CalendarContract.Calendars._ID))
            } else null
        }
    }

    private fun millisToLocalDate(millis: Long): LocalDate =
        Instant.fromEpochMilliseconds(millis).toLocalDateTime(tz).date

    private fun millisToLocalTime(millis: Long): LocalTime =
        Instant.fromEpochMilliseconds(millis).toLocalDateTime(tz).time

    private fun localDateTimeToMillis(date: LocalDate, time: LocalTime): Long =
        kotlinx.datetime.LocalDateTime(date, time).toInstant(tz).toEpochMilliseconds()

    private companion object {
        const val DAY_MILLIS = 24L * 60 * 60 * 1000
    }
}
