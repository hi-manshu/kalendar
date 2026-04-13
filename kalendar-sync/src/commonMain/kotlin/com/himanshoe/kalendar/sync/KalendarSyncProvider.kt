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
 * Reads, writes, and exports events from the device's built-in calendar without any
 * third-party API or cloud account.
 *
 * - **Android** — backed by [CalendarContract][android.provider.CalendarContract].
 *   Requires `READ_CALENDAR` and `WRITE_CALENDAR` permissions in your manifest.
 * - **iOS** — backed by [EventKit](https://developer.apple.com/documentation/eventkit).
 *   Requires `NSCalendarUsageDescription` in your `Info.plist`.
 * - **Desktop / Web** — [fetchEvents], [insertEvent], [deleteEvent] return
 *   [KalendarSyncResult.NotSupported]. Use the top-level [exportToIcs] and [importFromIcs]
 *   functions from `KalendarIcs.kt` for ICS serialization on all platforms.
 *
 * Obtain a platform instance via the `KalendarSync` factory function:
 * ```kotlin
 * // Android (inside Activity / ViewModel)
 * val sync = KalendarSync(context)
 *
 * // iOS / Desktop / Web
 * val sync = KalendarSync()
 * ```
 */
interface KalendarSyncProvider {

    /**
     * Checks whether the app currently holds the necessary calendar permission.
     *
     * On Android this is a synchronous permission check.
     * On iOS this triggers the system permission dialog the first time it is called.
     *
     * @return `true` if permission is already granted, `false` otherwise.
     */
    suspend fun hasPermission(): Boolean

    /**
     * Returns all events from the device calendar whose date falls within
     * [[startDate], [endDate]] (both inclusive).
     */
    suspend fun fetchEvents(
        startDate: LocalDate,
        endDate: LocalDate,
    ): KalendarSyncResult<List<KalendarSyncEvent>>

    /**
     * Inserts [event] into the default device calendar.
     *
     * @return [KalendarSyncResult.Success] carrying the new event's device-assigned ID.
     */
    suspend fun insertEvent(event: KalendarSyncEvent): KalendarSyncResult<String>

    /**
     * Updates the device-calendar event identified by [eventId] with the fields from [event].
     *
     * [eventId] must be the value returned by [insertEvent] or read from
     * [KalendarSyncEvent.id] via [fetchEvents]. The event's own [KalendarSyncEvent.id] field
     * is ignored — only [eventId] is used to locate the record.
     *
     * @return [KalendarSyncResult.Success] with [Unit] on success.
     */
    suspend fun updateEvent(eventId: String, event: KalendarSyncEvent): KalendarSyncResult<Unit>

    /**
     * Deletes the event identified by [eventId] from the device calendar.
     *
     * [eventId] must be the value returned by [insertEvent] or read from
     * [KalendarSyncEvent.id] via [fetchEvents].
     */
    suspend fun deleteEvent(eventId: String): KalendarSyncResult<Unit>

}
