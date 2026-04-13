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
 * Web/wasmJS stub. Browsers have no direct device-calendar API.
 *
 * Use the top-level [exportToIcs] function to produce an `.ics` string, then
 * trigger a file download so the user can import it into Google Calendar or
 * Apple Calendar manually.
 */
fun KalendarSync(): KalendarSyncProvider = WasmKalendarSync()

private class WasmKalendarSync : KalendarSyncProvider {
    override suspend fun hasPermission() = false

    override suspend fun fetchEvents(
        startDate: LocalDate,
        endDate: LocalDate,
    ): KalendarSyncResult<List<KalendarSyncEvent>> = KalendarSyncResult.NotSupported

    override suspend fun insertEvent(event: KalendarSyncEvent): KalendarSyncResult<String> =
        KalendarSyncResult.NotSupported

    override suspend fun updateEvent(eventId: String, event: KalendarSyncEvent): KalendarSyncResult<Unit> =
        KalendarSyncResult.NotSupported

    override suspend fun deleteEvent(eventId: String): KalendarSyncResult<Unit> =
        KalendarSyncResult.NotSupported
}
