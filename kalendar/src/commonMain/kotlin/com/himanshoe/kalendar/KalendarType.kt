/*
 *
 *  * Copyright 2025 Kalendar Contributors (https://www.himanshoe.com). All rights reserved.
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

/*
 * Copyright 2025 Kalendar Contributors (https://www.himanshoe.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/**
 * Represents the visual layout and interaction style of the Kalendar composable.
 *
 * Each type renders dates in a different format:
 * - [Firey] — A single-week row with arrow-based navigation.
 * - [Oceanic] — A full month grid with arrow-based navigation.
 * - [Aerial] — A single-week row with horizontal swipe (pager) navigation.
 * - [Solaris] — A full month grid with horizontal swipe (pager) navigation.
 * - [Cosmic] — An annual year view showing 12 mini-months with animated drill-down
 *   into month and week views.
 */
sealed interface KalendarType {
    /**
     * A week view with left/right arrow buttons for navigating between weeks.
     */
    data object Firey : KalendarType

    /**
     * A month view with left/right arrow buttons for navigating between months.
     */
    data object Oceanic : KalendarType

    /**
     * A week view with horizontal swipe (pager) navigation between weeks.
     * Includes a "today" icon button to jump back to the current week.
     */
    data object Aerial : KalendarType

    /**
     * A month view with horizontal swipe (pager) navigation between months.
     * Includes a "today" icon button to jump back to the current month.
     */
    data object Solaris : KalendarType

    /**
     * An annual year view displaying all 12 months in a compact 4×3 grid.
     *
     * Supports configurable animated drill-down navigation:
     * tapping a mini-month can open a full month view, and tapping a week
     * row in the month view can open a week view. All transitions use
     * smooth slide + fade animations.
     *
     * Use [com.himanshoe.kalendar.cosmic.KalendarCosmic] directly for full
     * configuration via [com.himanshoe.kalendar.cosmic.KalendarCosmicKonfig].
     */
    data object Cosmic : KalendarType
}
