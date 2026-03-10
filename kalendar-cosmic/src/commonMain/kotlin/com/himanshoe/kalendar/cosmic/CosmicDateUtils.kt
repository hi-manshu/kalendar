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

package com.himanshoe.kalendar.cosmic

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * Computes all dates to display for a given month, including leading days
 * from the previous month to align with the [startDayOfWeek].
 *
 * @param currentMonth A [LocalDate] representing the first day of the month.
 * @param startDayOfWeek The day of the week to use as the first column.
 * @return A list of [LocalDate] covering the full grid (including overflow from the previous month).
 */
internal fun getMonthDates(
    currentMonth: LocalDate,
    startDayOfWeek: DayOfWeek,
): List<LocalDate> {
    val firstDayOfMonth = currentMonth.minus(currentMonth.dayOfMonth - 1, DateTimeUnit.DAY)
    val lastDayOfMonth = firstDayOfMonth.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
    val firstDayOffset = (firstDayOfMonth.dayOfWeek.ordinal - startDayOfWeek.ordinal + 7) % 7
    return (-firstDayOffset until lastDayOfMonth.dayOfMonth).map {
        firstDayOfMonth.plus(it.toLong(), DateTimeUnit.DAY)
    }
}

/**
 * Computes the seven dates of the week containing [currentDay],
 * starting from the given [startDayOfWeek].
 *
 * @param currentDay Any date within the desired week.
 * @param startDayOfWeek The day of the week to use as the first column.
 * @return A list of seven [LocalDate] values for the week.
 */
internal fun getWeekDates(
    currentDay: LocalDate,
    startDayOfWeek: DayOfWeek,
): List<LocalDate> {
    val startOfWeek = currentDay.minus(
        (currentDay.dayOfWeek.ordinal - startDayOfWeek.ordinal + 7) % 7,
        DateTimeUnit.DAY,
    )
    return (0..6).map { startOfWeek.plus(it, DateTimeUnit.DAY) }
}

/**
 * Rotates this list of [DayOfWeek] so that the element at [distance] becomes the first element.
 *
 * @param distance The number of positions to rotate.
 * @return A new list with the elements rotated.
 */
internal fun List<DayOfWeek>.rotate(distance: Int): List<DayOfWeek> {
    return this.drop(distance) + this.take(distance)
}
