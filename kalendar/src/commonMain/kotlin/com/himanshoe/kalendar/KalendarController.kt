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

package com.himanshoe.kalendar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import kotlinx.datetime.LocalDate

/**
 * Provides imperative control over a [Kalendar] composable, enabling the host to programmatically
 * navigate to any date.
 *
 * Obtain an instance via [rememberKalendarController] and pass it to [Kalendar]:
 *
 * ```kotlin
 * val controller = rememberKalendarController()
 * val scope = rememberCoroutineScope()
 *
 * Kalendar(
 *     type = KalendarType.Oceanic,
 *     controller = controller,
 * )
 *
 * Button(onClick = {
 *     scope.launch { controller.scrollToDate(LocalDate(2026, 12, 25)) }
 * }) {
 *     Text("Go to Christmas")
 * }
 * ```
 *
 * `scrollToDate` is a `suspend` function that returns after the navigation completes — for
 * pager-based types ([KalendarType.Aerial], [KalendarType.Solaris]) this means after the
 * scroll animation finishes; for arrow-based types it returns immediately.
 *
 * Navigation semantics depend on the active [KalendarType]:
 * - **Oceanic / Solaris** — jumps to the month containing the target date.
 * - **Firey / Aerial** — jumps to the week containing the target date.
 */
@Stable
class KalendarController internal constructor() {

    private var scrollImpl: (suspend (LocalDate) -> Unit)? = null
    private var attachGeneration: Int = 0

    /**
     * Registers the scroll implementation provided by the attached [Kalendar] composable.
     * Called internally via `DisposableEffect` when the composable enters composition.
     *
     * @return A generation token that must be passed to [detachScrollImpl] so a newer
     * attach is not cleared when an older calendar leaves composition.
     */
    internal fun attachScrollImpl(impl: suspend (LocalDate) -> Unit): Int {
        val generation = ++attachGeneration
        scrollImpl = impl
        return generation
    }

    /**
     * Clears the scroll implementation when the attached [Kalendar] composable leaves
     * composition, but only if [generation] is still the active attachment.
     */
    internal fun detachScrollImpl(generation: Int) {
        if (generation == attachGeneration) {
            scrollImpl = null
        }
    }

    /**
     * Navigates the attached [Kalendar] so that [date] is visible.
     *
     * This function suspends until the navigation is complete. For animated pager-based types
     * it suspends for the duration of the scroll animation. For arrow-based types it returns
     * immediately after updating the displayed month or week.
     *
     * If no [Kalendar] is currently attached, this call is a no-op.
     */
    suspend fun scrollToDate(date: LocalDate) {
        scrollImpl?.invoke(date)
    }
}

/**
 * Creates and [remember]s a [KalendarController] that survives recomposition.
 */
@Composable
fun rememberKalendarController(): KalendarController = remember { KalendarController() }
