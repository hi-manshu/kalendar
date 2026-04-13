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
 * The result of a [KalendarSyncProvider] operation.
 *
 * Handle each outcome with a `when` expression:
 * ```kotlin
 * when (val result = sync.fetchEvents(startDate, endDate)) {
 *     is KalendarSyncResult.Success       -> show(result.data)
 *     is KalendarSyncResult.PermissionDenied -> askForPermission()
 *     is KalendarSyncResult.NotSupported  -> showUnsupportedMessage()
 *     is KalendarSyncResult.Error         -> showError(result.message)
 * }
 * ```
 */
sealed class KalendarSyncResult<out T> {

    /**
     * The operation completed successfully.
     *
     * @param data The value produced by the operation.
     */
    data class Success<T>(val data: T) : KalendarSyncResult<T>()

    /**
     * The required calendar permission was not granted by the user.
     *
     * On Android, direct the user to grant `READ_CALENDAR` / `WRITE_CALENDAR`
     * via `ActivityCompat.requestPermissions`.
     * On iOS, direct the user to Settings → Privacy → Calendars.
     */
    data object PermissionDenied : KalendarSyncResult<Nothing>()

    /**
     * Device calendar access is not available on this platform
     * (e.g. Desktop JVM, Web/wasmJS).
     */
    data object NotSupported : KalendarSyncResult<Nothing>()

    /**
     * The operation failed with an error.
     *
     * @param message A human-readable description of what went wrong.
     * @param cause The underlying exception, if available.
     */
    data class Error(
        val message: String,
        val cause: Throwable? = null,
    ) : KalendarSyncResult<Nothing>()
}
