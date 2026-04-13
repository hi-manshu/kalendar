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

/**
 * Selects the visual variant rendered by [Kalendar].
 *
 * | Type | Layout | Navigation |
 * |---|---|---|
 * | [Oceanic] | Full month grid | Arrow buttons |
 * | [Firey] | Single week row | Arrow buttons |
 * | [Solaris] | Full month grid | Horizontal swipe |
 * | [Aerial] | Single week row | Horizontal swipe |
 * | [Yearly] | 12-month year overview | Arrow buttons |
 * | [Agenda] | Event list grouped by date | Scroll |
 */
sealed interface KalendarType {
    /**
     * A static week-row calendar navigated with previous/next arrow buttons.
     */
    data object Firey : KalendarType

    /**
     * A static full-month grid calendar navigated with previous/next arrow buttons.
     */
    data object Oceanic : KalendarType

    /**
     * A swipeable week-row calendar. Users swipe horizontally to move between weeks.
     * A "return to today" icon appears whenever the user has swiped away from the current week.
     */
    data object Aerial : KalendarType

    /**
     * A swipeable full-month grid calendar. Users swipe horizontally to move between months.
     * A "return to today" icon appears whenever the user has swiped away from the current month.
     */
    data object Solaris : KalendarType

    /**
     * A year-overview calendar showing all 12 months in a compact scrollable grid.
     * Navigate between years with previous/next arrow buttons. Tapping a day fires
     * [onDaySelectionAction] and highlights the selected date.
     */
    data object Yearly : KalendarType

    /**
     * An agenda-style calendar that renders a scrollable list of events grouped by date.
     * Events within the same day are sorted by [com.himanshoe.kalendar.foundation.event.KalendarEvent.startTime]
     * when available. An empty-state message is shown when no events are provided.
     */
    data object Agenda : KalendarType
}
