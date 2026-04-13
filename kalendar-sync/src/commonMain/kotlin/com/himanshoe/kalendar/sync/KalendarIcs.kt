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

/**
 * Encodes [events] to a complete iCalendar (.ics) string compatible with Google Calendar,
 * Apple Calendar, Outlook, and any RFC 5545–compliant application.
 *
 * This is a pure in-memory operation — no [KalendarSyncProvider] or calendar permission required,
 * and it works on all platforms (Android, iOS, Desktop, Web).
 *
 * ```kotlin
 * val icsText = exportToIcs(events)
 * ```
 */
fun exportToIcs(events: List<KalendarSyncEvent>): String = KalendarICalCodec.encode(events)

/**
 * Parses an iCalendar (.ics) string and returns the events it contains.
 *
 * This is a pure in-memory operation — no [KalendarSyncProvider] or calendar permission required,
 * and it works on all platforms (Android, iOS, Desktop, Web).
 *
 * ```kotlin
 * val events = importFromIcs(icsText)
 * ```
 */
fun importFromIcs(icsContent: String): List<KalendarSyncEvent> = KalendarICalCodec.decode(icsContent)
