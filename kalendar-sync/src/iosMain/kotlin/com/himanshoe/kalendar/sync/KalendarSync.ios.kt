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

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import platform.EventKit.EKAuthorizationStatus
import platform.EventKit.EKEntityType
import platform.EventKit.EKEvent
import platform.EventKit.EKEventStore
import platform.EventKit.EKSpan
import platform.Foundation.NSDate
import platform.Foundation.NSError
import platform.Foundation.timeIntervalSince1970
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Creates an iOS [KalendarSyncProvider] backed by EventKit.
 *
 * **Required permission** — add to your `Info.plist`:
 * ```xml
 * <key>NSCalendarUsageDescription</key>
 * <string>This app needs calendar access to display and manage your events.</string>
 * ```
 */
fun KalendarSync(): KalendarSyncProvider = IosKalendarSync()

@OptIn(ExperimentalForeignApi::class)
private class IosKalendarSync : KalendarSyncProvider {

    private val eventStore = EKEventStore()
    private val tz get() = TimeZone.currentSystemDefault()

    override suspend fun hasPermission(): Boolean {
        val status = EKEventStore.authorizationStatusForEntityType(EKEntityType.EKEntityTypeEvent)
        if (status == EKAuthorizationStatus.EKAuthorizationStatusAuthorized) return true

        return suspendCoroutine { cont ->
            eventStore.requestAccessToEntityType(EKEntityType.EKEntityTypeEvent) { granted, _ ->
                cont.resume(granted)
            }
        }
    }

    override suspend fun fetchEvents(
        startDate: LocalDate,
        endDate: LocalDate,
    ): KalendarSyncResult<List<KalendarSyncEvent>> {
        if (!hasPermission()) return KalendarSyncResult.PermissionDenied
        return runCatching {
            val start = startDate.atStartOfDayIn(tz).toNSDate()
            val end = endDate.atStartOfDayIn(tz).toNSDate()
            val calendars = eventStore.calendarsForEntityType(EKEntityType.EKEntityTypeEvent)

            @Suppress("UNCHECKED_CAST")
            val ekEvents = eventStore.eventsMatchingPredicate(
                eventStore.predicateForEventsWithStartDate(start, end, calendars)
            ) as? List<EKEvent> ?: emptyList()

            KalendarSyncResult.Success(ekEvents.map { it.toKalendarSyncEvent() })
        }.getOrElse { KalendarSyncResult.Error("Failed to fetch events", it) }
    }

    override suspend fun insertEvent(event: KalendarSyncEvent): KalendarSyncResult<String> {
        if (!hasPermission()) return KalendarSyncResult.PermissionDenied
        return runCatching {
            val ekEvent = EKEvent.eventWithEventStore(eventStore).apply {
                title = event.eventName
                notes = event.eventDescription
                allDay = event.isAllDay
                startDate = if (event.isAllDay) {
                    event.date.atStartOfDayIn(tz).toNSDate()
                } else {
                    localTimeToNSDate(event.date, event.startTime ?: LocalTime(0, 0))
                }
                endDate = if (event.isAllDay) {
                    event.date.atStartOfDayIn(tz).plusDayNSDate()
                } else {
                    localTimeToNSDate(event.date, event.endTime ?: LocalTime(23, 59))
                }
                calendar = eventStore.defaultCalendarForNewEvents
            }

            suspendCoroutine<KalendarSyncResult<String>> { cont ->
                val error: NSError? = null
                val saved = eventStore.saveEvent(ekEvent, EKSpan.EKSpanThisEvent, true, null)
                if (saved) {
                    cont.resume(KalendarSyncResult.Success(ekEvent.eventIdentifier ?: ""))
                } else {
                    cont.resume(KalendarSyncResult.Error("EventKit saveEvent returned false"))
                }
            }
        }.getOrElse { KalendarSyncResult.Error("Failed to insert event", it) }
    }

    override suspend fun updateEvent(eventId: String, event: KalendarSyncEvent): KalendarSyncResult<Unit> {
        if (!hasPermission()) return KalendarSyncResult.PermissionDenied
        return runCatching {
            val ekEvent = eventStore.eventWithIdentifier(eventId)
                ?: return KalendarSyncResult.Error("Event with id=$eventId not found")
            ekEvent.title = event.eventName
            ekEvent.notes = event.eventDescription
            ekEvent.allDay = event.isAllDay
            ekEvent.startDate = if (event.isAllDay) {
                event.date.atStartOfDayIn(tz).toNSDate()
            } else {
                localTimeToNSDate(event.date, event.startTime ?: LocalTime(0, 0))
            }
            ekEvent.endDate = if (event.isAllDay) {
                event.date.atStartOfDayIn(tz).plusDayNSDate()
            } else {
                localTimeToNSDate(event.date, event.endTime ?: LocalTime(23, 59))
            }
            val saved = eventStore.saveEvent(ekEvent, EKSpan.EKSpanThisEvent, true, null)
            if (saved) KalendarSyncResult.Success(Unit)
            else KalendarSyncResult.Error("EventKit saveEvent returned false for update")
        }.getOrElse { KalendarSyncResult.Error("Failed to update event", it) }
    }

    override suspend fun deleteEvent(eventId: String): KalendarSyncResult<Unit> {
        if (!hasPermission()) return KalendarSyncResult.PermissionDenied
        return runCatching {
            val ekEvent = eventStore.eventWithIdentifier(eventId)
                ?: return KalendarSyncResult.Error("Event with id=$eventId not found")
            val deleted = eventStore.removeEvent(ekEvent, EKSpan.EKSpanThisEvent, true, null)
            if (deleted) KalendarSyncResult.Success(Unit)
            else KalendarSyncResult.Error("EventKit removeEvent returned false")
        }.getOrElse { KalendarSyncResult.Error("Failed to delete event", it) }
    }

    /**
     * Converts an [EKEvent] to a [BasicKalendarSyncEvent], using today's date as a fallback
     * when [EKEvent.startDate] is null.
     */
    private fun EKEvent.toKalendarSyncEvent(): KalendarSyncEvent {
        val date = startDate?.toLocalDate() ?: return BasicKalendarSyncEvent(
            id = eventIdentifier,
            date = kotlinx.datetime.Clock.System.now().toLocalDateTime(tz).date,
            eventName = title ?: "",
        )
        return BasicKalendarSyncEvent(
            id = eventIdentifier,
            date = date,
            eventName = title ?: "",
            eventDescription = notes?.takeIf { it.isNotBlank() },
            startTime = if (allDay) null else startDate?.toLocalTime(),
            endTime = if (allDay) null else endDate?.toLocalTime(),
        )
    }

    /** Converts a [kotlinx.datetime.Instant] to an [NSDate] for use with EventKit APIs. */
    private fun Instant.toNSDate(): NSDate =
        NSDate.dateWithTimeIntervalSince1970(epochSeconds.toDouble())

    /** Converts an [NSDate] to a [LocalDate] using the device's current time zone. */
    private fun NSDate.toLocalDate(): LocalDate =
        Instant.fromEpochSeconds(timeIntervalSince1970.toLong())
            .toLocalDateTime(tz).date

    /** Converts an [NSDate] to a [LocalTime] using the device's current time zone. */
    private fun NSDate.toLocalTime(): LocalTime =
        Instant.fromEpochSeconds(timeIntervalSince1970.toLong())
            .toLocalDateTime(tz).time

    /** Combines a [LocalDate] and [LocalTime] into an [NSDate] for use with EventKit APIs. */
    private fun localTimeToNSDate(date: LocalDate, time: LocalTime): NSDate {
        val instant = kotlinx.datetime.LocalDateTime(date, time).toInstant(tz)
        return NSDate.dateWithTimeIntervalSince1970(instant.epochSeconds.toDouble())
    }

    /** Returns the [Instant] at midnight of this [LocalDate] in the given [TimeZone]. */
    private fun LocalDate.atStartOfDayIn(tz: TimeZone): Instant =
        kotlinx.datetime.LocalDateTime(this, LocalTime(0, 0)).toInstant(tz)

    /** Returns an [NSDate] exactly 24 hours after this [Instant], used for all-day event end dates. */
    private fun Instant.plusDayNSDate(): NSDate =
        NSDate.dateWithTimeIntervalSince1970((epochSeconds + 86_400).toDouble())
}
